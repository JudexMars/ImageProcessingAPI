package org.judexmars.imageprocessor.service

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.filter.InvertFilter
import com.sksamuel.scrimage.nio.ImageWriter
import com.sksamuel.scrimage.nio.JpegWriter
import com.sksamuel.scrimage.nio.PngWriter
import jakarta.annotation.PostConstruct
import org.judexmars.imageprocessor.config.ProcessorProperties
import org.judexmars.imageprocessor.dto.ImageStatusMessage
import org.judexmars.imageprocessor.dto.S3ImageDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.util.UUID

@Service
@ConditionalOnProperty(name = ["props.type"], havingValue = "REVERSE_COLORS")
class ReverseColorsProcessor(
    val s3Service: S3Service,
    val template: KafkaTemplate<String, ImageStatusMessage>,
    val properties: ProcessorProperties,
) : Processor {
    private companion object {
        val log: Logger = LoggerFactory.getLogger(ReverseColorsProcessor::class.java)
    }

    @PostConstruct
    fun init() {
        log.info("Processor initialized: ${properties.type}")
        log.info("Class: ${this::class.java}")
    }

    override fun process(message: ImageStatusMessage) {
        val filters = message.filters
        if (filters.isEmpty() || filters.first() != "REVERSE_COLORS") return

        val source: S3ImageDto =
            try {
                s3Service.downloadImage(message.imageId.toString(), true)
            } catch (e: Exception) {
                s3Service.downloadImage(message.imageId.toString(), false)
            }

        val modifiedImage = reverseColors(source.bytes, source.contentType)

        val nextFilters = filters.drop(1)
        val isIntermediate = nextFilters.isNotEmpty()
        val nextTopic = if (isIntermediate) properties.wipTopic else properties.doneTopic

        val modifiedS3Image =
            s3Service.uploadImage(
                modifiedImage,
                source.contentType,
                modifiedImage.size.toLong(),
                isIntermediate,
            )

        val nextMessage =
            message.copy(
                imageId = UUID.fromString(modifiedS3Image.link),
                filters = nextFilters,
            )
        template.send(nextTopic, nextMessage)
    }

    fun reverseColors(
        source: ByteArray,
        contentType: String,
    ): ByteArray {
        val inputStream = ByteArrayInputStream(source)
        val image = ImmutableImage.loader().fromStream(inputStream)

        val invertedImage = image.filter(InvertFilter())

        val writer: ImageWriter =
            when (contentType) {
                "image/jpeg" -> JpegWriter.CompressionFromMetaData
                "image/png" -> PngWriter.NoCompression
                else -> throw IllegalArgumentException()
            }
        return invertedImage.bytes(writer)
    }
}
