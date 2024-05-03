package org.judexmars.imagecrud.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.judexmars.imagecrud.dto.kafka.ImageStatusMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for images.
 */
@Slf4j
@Component
public class KafkaConsumer {

  /**
   * Consume message from kafka.
   *
   * @param record         consumable message
   * @param acknowledgment commit
   */
  @KafkaListener(
      topics = "images.done",
      groupId = "consumers-group-1",
      concurrency = "2",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void consume(ConsumerRecord<String, ImageStatusMessage> record,
                      Acknowledgment acknowledgment) {
    log.info("""
        Получено следующее сообщение из топика {}:
        key: {},
        value: {}
        """, record.topic(), record.key(), record.value());
    acknowledgment.acknowledge();
  }
}
