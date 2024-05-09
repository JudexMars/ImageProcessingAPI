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
@ConditionalOnProperty(name = ["props.type"], havingValue = "CROP")
class CropProcessor(
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
        log.info("Crop processor started processing new message: $message")
        val filters = message.filters
        if (filters.isEmpty() || filters.first() != "CROP") return

        val props = message.props

        val x1 = props["x1"]
        val y1 = props["y1"]
        val x2 = props["x2"]
        val y2 = props["y2"]

        if (x1 != null && y1 != null && x2 != null && y2 != null) {
            val source: S3ImageDto =
                try {
                    s3Service.downloadImage(message.imageId.toString(), true)
                } catch (e: Exception) {
                    s3Service.downloadImage(message.imageId.toString(), false)
                }
            val modifiedImage =
                crop(
                    source.bytes,
                    source.contentType,
                    x1.toInt(),
                    y1.toInt(),
                    x2.toInt(),
                    y2.toInt(),
                )

            val nextFilters = filters.drop(1)
            val isIntermediate = nextFilters.isNotEmpty()
            val nextProps =
                props.filterKeys {
                    !it.contains("x") &&
                        !it.contains("y")
                }
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
                    props = nextProps,
                )
            template.send(nextTopic, nextMessage)
        }
    }

    fun crop(
        source: ByteArray,
        contentType: String,
        x1: Int,
        y1: Int,
        x2: Int,
        y2: Int,
    ): ByteArray {
        val inputStream = ByteArrayInputStream(source)
        val image = ImmutableImage.loader().fromStream(inputStream)

        val croppedImage =
            image.subimage(
                x1,
                y1,
                x2 - x1,
                y2 - y1,
            )

        val writer: ImageWriter =
            when (contentType) {
                "image/jpeg" -> JpegWriter.CompressionFromMetaData
                "image/png" -> PngWriter.NoCompression
                else -> throw IllegalArgumentException()
            }
        return croppedImage.bytes(writer)
    }
}
