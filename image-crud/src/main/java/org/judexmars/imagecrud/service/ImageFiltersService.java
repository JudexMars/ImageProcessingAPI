package org.judexmars.imagecrud.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.judexmars.imagecrud.dto.imagefilters.ApplyImageFiltersResponseDto;
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
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.JsonDeserializer;
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
   * @param imageId id of the image
   * @param filters filter types
   * @param accountId id of account requesting this
   * @return DTO with request id
   */
  public ApplyImageFiltersResponseDto applyFilters(UUID imageId,
                                                   List<FilterType> filters,
                                                   Map<String, String> props,
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
    return new ApplyImageFiltersResponseDto(savedRequest.getRequestId());
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
    var image = request.getImage();
    var modifiedImage = request.getModifiedImage();
    if (modifiedImage != null) {
      image = modifiedImage;
    }
    return new GetModifiedImageDto(image.getId(),
        BasicRequestStatus.valueOf(request.getStatus().getName()));
  }

  /**
   * Kafka consumer which updates requests in the DB according to incoming messages.
   *
   * @param record incoming message from Kafka broker
   */
  @KafkaListener(
      topics = "images.done",
      groupId = "images-done-consumer-group-1",
      concurrency = "2",
      properties = {
          ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG
              + "=org.springframework.kafka.support.serializer.JsonDeserializer",
          JsonDeserializer.TRUSTED_PACKAGES + "=org.judexmars.imagecrud.dto.kafka",
          ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG + "=false",
          ConsumerConfig.ISOLATION_LEVEL_CONFIG + "=read_committed",
          ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG
              + "=org.apache.kafka.clients.consumer.RoundRobinAssignor"
      }
  )
  public void consumeDoneImage(ConsumerRecord<String, ImageStatusMessage> record,
                               Acknowledgment ack) {
    var value = record.value();
    var imageId = value.imageId();
    var requestId = value.requestId();
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
    ack.acknowledge();
  }

  private RequestStatus getRequestStatus(String name) {
    log.info("Getting request status for {}", name);
    return requestStatusRepository.findByName(name).orElseThrow(() ->
        new RequestNotFoundException(name));
  }

  private ApplyFilterRequestEntity getRequestEntity(UUID requestId) {
    return applyFilterRequestRepository.findById(requestId).orElseThrow(() ->
        new RequestNotFoundException(requestId.toString()));
  }
}