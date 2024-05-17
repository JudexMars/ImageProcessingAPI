package org.judexmars.imagecrud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.judexmars.imagecrud.dto.kafka.ImageStatusMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for images.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ModifiedImagesKafkaConsumer {

  private final ImageFiltersService imageFiltersService;

  /**
   * Kafka consumer which updates requests in the DB according to incoming messages.
   *
   * @param value incoming message from Kafka broker
   */
  @KafkaListener(
      topics = "images.done",
      groupId = "images-done-consumer-group-1",
      concurrency = "2",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void consumeDoneImage(ImageStatusMessage value,
                               Acknowledgment ack) {
    imageFiltersService.processDoneImage(value);

    ack.acknowledge();
  }
}
