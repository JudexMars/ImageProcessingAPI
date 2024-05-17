package org.judexmars.imagecrud.config.kafka;

import static org.springframework.kafka.core.KafkaAdmin.NewTopics;

import java.util.Map;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.judexmars.imagecrud.dto.kafka.ImageStatusMessage;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Kafka configuration.
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class KafkaConfiguration {

  private final KafkaProperties properties;

  /**
   * Register all required topics.
   *
   * @return kafka topics
   */
  @Bean
  public NewTopics topics() {
    return new KafkaAdmin.NewTopics(
        TopicBuilder.name("images.wip")
            .replicas(3)
            .partitions(2)
            .build(),
        TopicBuilder.name("images.done")
            .replicas(3)
            .partitions(2)
            .build());
  }

  @Bean("IMAGES_WIP_TEMPLATE")
  public KafkaTemplate<String, ImageStatusMessage> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory(props ->
        props.put(ProducerConfig.ACKS_CONFIG, "all")));
  }

  private <K, V> ProducerFactory<K, V> producerFactory(
      Consumer<Map<String, Object>> enchanter) {
    var props = properties.buildProducerProperties(null);

    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

    props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, RoundRobinPartitioner.class);

    props.put(ProducerConfig.LINGER_MS_CONFIG, 0);

    props.put(ProducerConfig.RETRIES_CONFIG, 3);

    enchanter.accept(props);

    return new DefaultKafkaProducerFactory<>(props);
  }

  /**
   * Factory for kafka consumer.
   *
   * @return {@link ConcurrentKafkaListenerContainerFactory}
   */
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, ImageStatusMessage>
          kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, ImageStatusMessage> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
    factory.setAutoStartup(true);
    factory.setConcurrency(2);
    factory.setConsumerFactory(consumerFactory());
    factory.setCommonErrorHandler(errorHandler());
    return factory;
  }

  private <K, V> ConsumerFactory<K, V> consumerFactory() {
    var props = properties.buildConsumerProperties(null);

    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

    props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,
        RoundRobinAssignor.class.getName());
    props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ImageStatusMessage.class);
    props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

    return new DefaultKafkaConsumerFactory<>(props);
  }

  private DefaultErrorHandler errorHandler() {
    var fixedBackOff = new FixedBackOff(1000, 3);
    return new DefaultErrorHandler((consumerRecord, exception)
        -> log.info("Consumer error for record: {}", consumerRecord), fixedBackOff);
  }
}
