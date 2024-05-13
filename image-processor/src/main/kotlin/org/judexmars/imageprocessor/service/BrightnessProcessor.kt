package org.judexmars.imageprocessor.service

import com.sksamuel.scrimage.ImmutableImage
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
@ConditionalOnProperty(name = ["props.type"], havingValue = "BRIGHTNESS")
class BrightnessProcessor(
    val s3Service: S3Service,
    val template: KafkaTemplate<String, ImageStatusMessage>,
    val properties: ProcessorProperties,
) : Processor {
    companion object {
        val log: Logger = LoggerFactory.getLogger(BrightnessProcessor::class.java)
    }

    @PostConstruct
    fun init() {
        log.info("Processor initialized: ${properties.type}")
        log.info("Class: ${this::class.java}")
    }

    override fun process(message: ImageStatusMessage) {
        log.info("Brightness processor started processing new message: $message")
        val filters = message.filters
        if (filters.isEmpty() || filters.first() != "BRIGHTNESS") return

        val props = message.props

        val brightness = props["brightness"]

        var nextImageId = message.imageId
        val nextFilters = filters.drop(1)
        val isIntermediate = nextFilters.isNotEmpty()

        if (brightness != null) {
            val source: S3ImageDto =
                try {
                    s3Service.downloadImage(message.imageId.toString(), true)
                } catch (e: Exception) {
                    s3Service.downloadImage(message.imageId.toString(), false)
                }
            val modifiedImage =
                adjustBrightness(
                    source.bytes,
                    source.contentType,
                    brightness.toDouble(),
                )

            val modifiedS3Image =
                s3Service.uploadImage(
                    modifiedImage,
                    source.contentType,
                    modifiedImage.size.toLong(),
                    isIntermediate,
                )
            nextImageId = UUID.fromString(modifiedS3Image.link)
        }

        val nextProps =
            props.filterKeys {
                !it.contains("brightness")
            }
        val nextTopic = if (isIntermediate) properties.wipTopic else properties.doneTopic

        val nextMessage =
            message.copy(
                imageId = nextImageId,
                filters = nextFilters,
                props = nextProps,
            )
        template.send(nextTopic, nextMessage)
    }

    fun adjustBrightness(
        source: ByteArray,
        contentType: String,
        brightness: Double,
    ): ByteArray {
        val inputStream = ByteArrayInputStream(source)
        val image = ImmutableImage.loader().fromStream(inputStream)

        val brightened = image.brightness(brightness)

        val writer: ImageWriter =
            when (contentType) {
                "image/jpeg" -> JpegWriter.CompressionFromMetaData
                "image/png" -> PngWriter.NoCompression
                else -> throw IllegalArgumentException()
            }
        return brightened.bytes(writer)
    }
}
