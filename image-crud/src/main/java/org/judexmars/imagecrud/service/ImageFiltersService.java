package org.judexmars.imagecrud.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.judexmars.imagecrud.dto.imagefilters.ApplyImageFiltersResponse;
import org.judexmars.imagecrud.dto.imagefilters.BasicRequestStatus;
import org.judexmars.imagecrud.dto.imagefilters.FilterType;
import org.judexmars.imagecrud.dto.imagefilters.GetModifiedImageDto;
import org.judexmars.imagecrud.dto.kafka.ImageStatusMessage;
import org.judexmars.imagecrud.exception.ImageNotFoundException;
import org.judexmars.imagecrud.exception.RequestNotFoundException;
import org.judexmars.imagecrud.model.ApplyFilterRequestEntity;
import org.judexmars.imagecrud.model.ImageEntity;
import org.judexmars.imagecrud.model.RequestStatus;
import org.judexmars.imagecrud.repository.ApplyFilterRequestRepository;
import org.judexmars.imagecrud.repository.ImageRepository;
import org.judexmars.imagecrud.repository.RequestStatusRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for applying filters to images.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImageFiltersService {

  private final KafkaTemplate<String, ImageStatusMessage> kafkaTemplate;

  private final ImageService imageService;

  private final ImageRepository imageRepository;

  private final ApplyFilterRequestRepository applyFilterRequestRepository;

  private final RequestStatusRepository requestStatusRepository;
  private final S3Service s3Service;

  /**
   * Applies filters to the selected image asynchronously.
   *
   * @param imageId   id of the image
   * @param filters   filter types
   * @param accountId id of account requesting this
   * @return DTO with request id
   */
  @Transactional
  public ApplyImageFiltersResponse applyFilters(UUID imageId,
                                                List<FilterType> filters,
                                                Map<String, Object> props,
                                                UUID accountId) {
    var meta = imageService.getImageMetaAsEntitySafely(imageId, accountId);
    var wipStatus = getRequestStatus(BasicRequestStatus.WIP.name());
    var request = new ApplyFilterRequestEntity().setStatus(wipStatus).setImage(meta);
    var savedRequest = applyFilterRequestRepository.save(request);
    kafkaTemplate.send("images.wip",
        new ImageStatusMessage(
            meta.getLink(),
            savedRequest.getRequestId().toString(),
            filters,
            props));
    return new ApplyImageFiltersResponse(savedRequest.getRequestId());
  }


  /**
   * Get apply image filter request.
   *
   * @param imageId   id of the image
   * @param requestId id of the request
   * @return DTO containing id of the modified image
   * (or the original if it hasn't been processed yet) and request status
   */
  @Transactional
  public GetModifiedImageDto getApplyImageFilterRequest(UUID imageId,
                                                        UUID requestId,
                                                        UUID accountId) {
    imageService.getImageMeta(imageId, accountId);
    var request = getRequestEntity(requestId);
    var image = request.getImage();
    var modifiedImage = request.getModifiedImage();
    if (modifiedImage != null) {
      image = modifiedImage;
    }
    return new GetModifiedImageDto(image.getId(),
        BasicRequestStatus.valueOf(request.getStatus().getName()));
  }

  /**
   * Process status message about done modified image.
   *
   * @param statusMessage arrived status message
   */
  public void processDoneImage(ImageStatusMessage statusMessage) {
    var imageId = statusMessage.imageId();
    var requestId = statusMessage.requestId();
    var request = getRequestEntity(UUID.fromString(requestId));
    var originalImage = imageService.getImageMetaAsEntity(request.getImage().getId());
    request.setStatus(getRequestStatus(BasicRequestStatus.DONE.name()));
    try {
      request.setModifiedImage(
          imageRepository.save(
              new ImageEntity()
                  .setAuthor(originalImage.getAuthor())
                  .setLink(imageId)
                  .setSize((int) s3Service.getImageSize(imageId))
                  .setFilename(requestId + "_" + originalImage.getFilename()))
      );
      applyFilterRequestRepository.save(request);
    } catch (Exception ex) {
      throw new ImageNotFoundException(imageId);
    }
  }

  private RequestStatus getRequestStatus(String name) {
    log.info("Getting request status for {}", name);
    return requestStatusRepository.findByName(name).orElseThrow(() ->
        new RequestNotFoundException(name));
  }

  /**
   * Get {@link ApplyFilterRequestEntity} by id.
   *
   * @param requestId id
   * @return entity
   */
  public ApplyFilterRequestEntity getRequestEntity(UUID requestId) {
    return applyFilterRequestRepository.findById(requestId).orElseThrow(() ->
        new RequestNotFoundException(requestId.toString()));
  }
}
