package org.judexmars.imagecrud.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.judexmars.imagecrud.dto.imagefilters.ApplyImageFiltersResponse;
import org.judexmars.imagecrud.dto.imagefilters.BasicRequestStatus;
import org.judexmars.imagecrud.dto.imagefilters.FilterType;
import org.judexmars.imagecrud.dto.imagefilters.GetModifiedImageDto;
import org.judexmars.imagecrud.dto.kafka.ImageStatusMessage;
import org.judexmars.imagecrud.exception.RequestNotFoundException;
import org.judexmars.imagecrud.model.ApplyFilterRequestEntity;
import org.judexmars.imagecrud.model.RequestStatus;
import org.judexmars.imagecrud.repository.ApplyFilterRequestRepository;
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

  private final ApplyFilterRequestRepository applyFilterRequestRepository;

  private final RequestStatusRepository requestStatusRepository;

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
                                                UUID accountId,
                                                List<FilterType> filters) {
    var meta = imageService.getImageMetaAsEntitySafely(imageId, accountId);
    var wipStatus = getRequestStatus(BasicRequestStatus.WIP.name());
    var request = new ApplyFilterRequestEntity().setStatus(wipStatus).setImage(meta);
    var savedRequest = applyFilterRequestRepository.save(request);
    kafkaTemplate.send("images.wip",
        new ImageStatusMessage(imageId, savedRequest.getRequestId(), filters));
    return new ApplyImageFiltersResponse(savedRequest.getRequestId());
  }


  /**
   * Get apply image filter request.
   *
   * @param imageId   id of the image
   * @param requestId id of the request
   * @return DTO containing id of the modified image
   *     (or the original if it hasn't been processed yet) and request status
   */
  public GetModifiedImageDto getApplyImageFilterRequest(UUID imageId,
                                                        UUID requestId,
                                                        UUID accountId) {
    imageService.getImageMeta(imageId, accountId);
    var request = getRequestEntity(requestId);
    return new GetModifiedImageDto(request.getImage().getId(),
        BasicRequestStatus.valueOf(request.getStatus().getName()));
  }

  /**
   * Get {@link RequestStatus} by name.
   *
   * @param name request name
   * @return entity
   */
  public RequestStatus getRequestStatus(String name) {
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

  /**
   * Save new apply filter request.
   *
   * @param request request to be saved
   */
  public void saveRequest(ApplyFilterRequestEntity request) {
    applyFilterRequestRepository.save(request);
  }

  /**
   * Process status message about done modified image.
   *
   * @param statusMessage arrived status message
   */
  public void processDoneImage(ImageStatusMessage statusMessage) {
    var imageId = statusMessage.imageId();
    var requestId = statusMessage.requestId();
    var request = getRequestEntity(requestId);
    request.setStatus(getRequestStatus(BasicRequestStatus.DONE.name()));
    request.setImage(imageService.getImageMetaAsEntity(imageId));
    saveRequest(request);
  }
}
