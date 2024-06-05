package org.judexmars.imageprocessor.service

import io.micrometer.core.annotation.Timed
import jakarta.annotation.PostConstruct
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
    @Timed(
        value = "imagefilter.time",
        description = "Execution time of filter processing",
        percentiles = [0.5, 0.95, 0.99],
        histogram = true,
    )
    fun listen(
        statusMessage: ImageStatusMessage,
        acknowledgment: Acknowledgment?,
    ) {
        val imageId = statusMessage.imageId.toString()
        val requestId = statusMessage.requestId.toString()
        val cachedImageId = redisTemplate.opsForValue().get(requestId)
        if (imageId != cachedImageId) {
            processors.forEach { it.process(statusMessage) }
            redisTemplate.opsForValue().set(
                requestId,
                imageId,
            )
            redisTemplate.expire(
                requestId,
                1L,
                TimeUnit.HOURS,
            )
        }
        acknowledgment?.acknowledge()
    }
}
