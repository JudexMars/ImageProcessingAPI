package org.judexmars.imageprocessor.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.ImageWriter
import com.sksamuel.scrimage.nio.JpegWriter
import com.sksamuel.scrimage.nio.PngWriter
import org.judexmars.imageprocessor.config.ProcessorProperties
import org.judexmars.imageprocessor.dto.ImageStatusMessage
import org.judexmars.imageprocessor.dto.props.CropProps
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.util.MimeTypeUtils
import java.io.ByteArrayInputStream

@Service
@ConditionalOnProperty(name = ["props.type"], havingValue = "CROP")
class CropProcessor(
    s3Service: S3Service,
    template: KafkaTemplate<String, ImageStatusMessage>,
    properties: ProcessorProperties,
    objectMapper: ObjectMapper = jacksonObjectMapper(),
) : AbstractProcessor<CropProps>(
        s3Service,
        template,
        properties,
        CropProps::class.java,
        objectMapper,
    ) {
    override fun applyFilter(
        source: ByteArray,
        contentType: String,
        props: CropProps?,
    ): ByteArray {
        if (props == null) return source
        val inputStream = ByteArrayInputStream(source)
        val image = ImmutableImage.loader().fromStream(inputStream)
        val croppedImage =
            image.subimage(
                props.x1,
                props.y1,
                props.x2 - props.x1,
                props.y2 - props.y1,
            )

        val writer: ImageWriter =
            when (contentType) {
                MimeTypeUtils.IMAGE_JPEG_VALUE -> JpegWriter.CompressionFromMetaData
                MimeTypeUtils.IMAGE_PNG_VALUE -> PngWriter.NoCompression
                else -> throw IllegalArgumentException("Unsupported content type: $contentType")
            }
        return croppedImage.bytes(writer)
    }
}
