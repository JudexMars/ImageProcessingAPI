package org.judexmars.imageprocessor.service

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.ImageWriter
import com.sksamuel.scrimage.nio.JpegWriter
import com.sksamuel.scrimage.nio.PngWriter
import org.judexmars.imageprocessor.config.ProcessorProperties
import org.judexmars.imageprocessor.config.ProcessorType
import org.judexmars.imageprocessor.dto.ImageStatusMessage
import org.judexmars.imageprocessor.dto.S3ImageDto
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.util.*

@Service
@ConditionalOnProperty(prefix = "props.type", name = ["CROP"])
class CropProcessor(
    val s3Service: S3Service,
    val template: KafkaTemplate<String, ImageStatusMessage>,
    val properties: ProcessorProperties
) : Processor {

    override fun process(message: ImageStatusMessage) {
        val filters = message.filters
        if (filters.first() != ProcessorType.CROP) return

        val props = message.props
        val source: S3ImageDto = try {
            s3Service.downloadImage(message.imageId.toString(), true)
        } catch (e: Exception) {
            s3Service.downloadImage(message.imageId.toString(), false)
        }

        val x1 = props["x1"]
        val y1 = props["y1"]
        val x2 = props["x2"]
        val y2 = props["y2"]

        if (x1 != null && y1 != null && x2 != null && y2 != null) {
            val modifiedImage = crop(
                source.bytes,
                source.contentType,
                x1.toInt(), y1.toInt(),
                x2.toInt(), y2.toInt()
            )
            val intermediateImage = s3Service.uploadImage(
                modifiedImage,
                source.contentType,
                modifiedImage.size.toLong(),
                true
            )

            val nextFilters = filters.drop(1)
            val nextProps = props.filterKeys {
                !it.contains("x") &&
                        !it.contains("y")
            }
            val nextTopic = if (nextFilters.isEmpty()) properties.doneTopic else properties.wipTopic
            val nextMessage = message.copy(
                imageId = UUID.fromString(intermediateImage.link),
                filters = nextFilters,
                props = nextProps
            )
            template.send(nextTopic, nextMessage)
        }
    }

    fun crop(
        source: ByteArray,
        contentType: String,
        x1: Int, y1: Int, x2: Int, y2: Int
    ): ByteArray {
        val inputStream = ByteArrayInputStream(source)
        val image = ImmutableImage.loader().fromStream(inputStream)

        val croppedImage = image.subimage(
            x1, y1,
            x2 - x1,
            y2 - y1
        )

        val writer: ImageWriter = when (contentType) {
            "JPG", "JPEG" -> JpegWriter().withCompression(100)
            "PNG" -> PngWriter().withCompression(100)
            else -> throw IllegalArgumentException()
        }
        return croppedImage.bytes(writer)
    }
}