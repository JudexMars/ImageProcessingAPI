package org.judexmars.imageprocessor.config.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.RoundRobinAssignor
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.RoundRobinPartitioner
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.judexmars.imageprocessor.config.ProcessorProperties
import org.judexmars.imageprocessor.dto.ImageStatusMessage
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ContainerProperties.AckMode
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
@EnableConfigurationProperties(KafkaProperties::class)
class KafkaConfiguration(
    private val properties: KafkaProperties,
) {
    @Bean
    fun producerFactory(): ProducerFactory<String, ImageStatusMessage> {
        val props = properties.buildProducerProperties(null).toMutableMap()

        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        props[ProducerConfig.PARTITIONER_CLASS_CONFIG] = RoundRobinPartitioner::class.java
        props[JsonSerializer.ADD_TYPE_INFO_HEADERS] = false
        props[ProducerConfig.LINGER_MS_CONFIG] = 0
        props[ProducerConfig.ACKS_CONFIG] = "all"
        props[ProducerConfig.RETRIES_CONFIG] = 3

        return DefaultKafkaProducerFactory(props)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, ImageStatusMessage> {
        return KafkaTemplate(producerFactory())
    }

    @Bean
    fun consumerFactory(processorProperties: ProcessorProperties): ConsumerFactory<String, ImageStatusMessage> {
        val props = properties.buildConsumerProperties(null).toMutableMap()

        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
        props[JsonDeserializer.VALUE_DEFAULT_TYPE] = ImageStatusMessage::class.java
        props[JsonDeserializer.USE_TYPE_INFO_HEADERS] = false
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        props[ConsumerConfig.GROUP_ID_CONFIG] = processorProperties.group
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        props[ConsumerConfig.ISOLATION_LEVEL_CONFIG] = "read_committed"
        props[ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG] = listOf(RoundRobinAssignor::class.java)
        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    fun kafkaListenerContainerFactory(
        processorProperties: ProcessorProperties,
    ): ConcurrentKafkaListenerContainerFactory<String, ImageStatusMessage> {
        return ConcurrentKafkaListenerContainerFactory<String, ImageStatusMessage>().apply {
            with(containerProperties) {
                ackMode = AckMode.MANUAL
                setAutoStartup(true)
                setConcurrency(processorProperties.concurrency)
                consumerFactory = consumerFactory(processorProperties)
            }
        }
    }
}
