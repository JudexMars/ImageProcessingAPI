package org.judexmars.imageprocessor.service

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.filter.InvertFilter
import com.sksamuel.scrimage.nio.ImageWriter
import com.sksamuel.scrimage.nio.JpegWriter
import com.sksamuel.scrimage.nio.PngWriter
import jakarta.annotation.PostConstruct
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.judexmars.imageprocessor.config.ProcessorProperties
import org.judexmars.imageprocessor.config.ProcessorType
import org.judexmars.imageprocessor.dto.ImageStatusMessage
import org.judexmars.imageprocessor.dto.S3ImageDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.util.*

@Service
@ConditionalOnProperty(name = ["props.type"], havingValue = "REVERSE_COLORS")
class ReverseColorsProcessor(
    val s3Service: S3Service,
    val template: KafkaTemplate<String, ImageStatusMessage>,
    val properties: ProcessorProperties
) : Processor {

    private companion object {
        val log: Logger = LoggerFactory.getLogger(ReverseColorsProcessor::class.java)
    }

    @PostConstruct
    fun init() {
        log.info("Processor initialized: ${properties.type}")
    }

    @KafkaListener(
        containerFactory = "kafkaListenerContainerFactory",
        topics = ["images.wip"]
    )
    fun listen(record: ConsumerRecord<String, ImageStatusMessage>, acknowledgment: Acknowledgment?) {
        log.info("Message consumed by ${properties.type}")
        process(record.value())
        acknowledgment?.acknowledge()
    }

    override fun process(message: ImageStatusMessage) {
        val filters = message.filters
        if (filters.first() != ProcessorType.REVERSE_COLORS) return

        val source: S3ImageDto = try {
            s3Service.downloadImage(message.imageId.toString(), true)
        } catch (e: Exception) {
            s3Service.downloadImage(message.imageId.toString(), false)
        }

        val modifiedImage = reverseColors(source.bytes, source.contentType)
        val intermediateImage = s3Service.uploadImage(
            modifiedImage,
            source.contentType,
            modifiedImage.size.toLong(),
            true
        )

        val nextFilters = filters.drop(1)
        val nextTopic = if (nextFilters.isEmpty()) properties.doneTopic else properties.wipTopic
        val nextMessage = message.copy(
            imageId = UUID.fromString(intermediateImage.link),
            filters = nextFilters
        )
        template.send(nextTopic, nextMessage)
    }

    fun reverseColors(source: ByteArray, contentType: String): ByteArray {
        val inputStream = ByteArrayInputStream(source)
        val image = ImmutableImage.loader().fromStream(inputStream)

        val invertedImage = image.filter(InvertFilter())

        val writer: ImageWriter = when (contentType) {
            "JPG", "JPEG" -> JpegWriter().withCompression(100)
            "PNG" -> PngWriter().withCompression(100)
            else -> throw IllegalArgumentException()
        }
        return invertedImage.bytes(writer)
    }
}