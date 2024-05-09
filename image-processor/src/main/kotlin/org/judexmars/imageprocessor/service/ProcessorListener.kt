package org.judexmars.imageprocessor.service

import jakarta.annotation.PostConstruct
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.judexmars.imageprocessor.dto.ImageStatusMessage
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class ProcessorListener(
    private val processors: List<Processor>,
    private val redisTemplate: RedisTemplate<String, String>,
) {
    companion object {
        private val log = LoggerFactory.getLogger(ProcessorListener::class.java)
    }

    @PostConstruct
    fun init() {
        log.info("Starting processor listener: $processors")
    }

    @KafkaListener(
        containerFactory = "kafkaListenerContainerFactory",
        topics = ["images.wip"],
    )
    fun listen(
        record: ConsumerRecord<String, ImageStatusMessage>,
        acknowledgment: Acknowledgment?,
    ) {
        val imageId = redisTemplate.opsForValue().get(record.value().requestId.toString())
        if (imageId != record.value().imageId.toString()) {
            processors.forEach { it.process(record.value()) }
            redisTemplate.opsForValue().set(
                record.value().requestId.toString(),
                record.value().imageId.toString(),
            )
            redisTemplate.expire(
                record.value().requestId.toString(),
                1L,
                TimeUnit.HOURS,
            )
        }
        acknowledgment?.acknowledge()
    }
}
