package org.judexmars.imagecrud.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
      properties = {
          ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG + "=false",
          ConsumerConfig.ISOLATION_LEVEL_CONFIG + "=read_committed",
          ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG
              + "=org.apache.kafka.clients.consumer.RoundRobinAssignor"
      }
  )
  public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
    log.info("""
        Получено следующее сообщение из топика {}:
        key: {},
        value: {}
        """, record.topic(), record.key(), record.value());
    acknowledgment.acknowledge();
  }
}
