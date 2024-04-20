package org.judexmars.imagecrud.config.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.apache.kafka.common.serialization.StringSerializer;
import org.judexmars.imagecrud.dto.kafka.ImageStatusMessage;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;
import java.util.function.Consumer;

import static org.springframework.kafka.core.KafkaAdmin.NewTopics;

/**
 * Kafka configuration.
 */
@Configuration
@RequiredArgsConstructor
public class KafkaInitializer {

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

        // Работаем со строками
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Партиция одна, так что все равно как роутить
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, RoundRobinPartitioner.class);

        // Отправляем сообщения сразу
        props.put(ProducerConfig.LINGER_MS_CONFIG, 0);

        // 3 попытки отправки сообщения
        props.put(ProducerConfig.RETRIES_CONFIG, 3);

        // До-обогащаем конфигурацию
        enchanter.accept(props);

        return new DefaultKafkaProducerFactory<>(props);
    }
}
