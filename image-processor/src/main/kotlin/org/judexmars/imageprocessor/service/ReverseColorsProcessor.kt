package org.judexmars.imageprocessor.service

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.filter.InvertFilter
import com.sksamuel.scrimage.nio.ImageWriter
import com.sksamuel.scrimage.nio.JpegWriter
import com.sksamuel.scrimage.nio.PngWriter
import org.judexmars.imageprocessor.config.ProcessorProperties
import org.judexmars.imageprocessor.dto.ImageStatusMessage
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.util.MimeTypeUtils
import java.io.ByteArrayInputStream

@Service
@ConditionalOnProperty(name = ["props.type"], havingValue = "REVERSE_COLORS")
class ReverseColorsProcessor(
    s3Service: S3Service,
    template: KafkaTemplate<String, ImageStatusMessage>,
    properties: ProcessorProperties,
) : AbstractProcessor<Any>(
        s3Service,
        template,
        properties,
    ) {
    override fun applyFilter(
        source: ByteArray,
        contentType: String,
        props: Any?,
    ): ByteArray {
        val inputStream = ByteArrayInputStream(source)
        val image = ImmutableImage.loader().fromStream(inputStream)

        val invertedImage = image.filter(InvertFilter())

        val writer: ImageWriter =
            when (contentType) {
                MimeTypeUtils.IMAGE_JPEG_VALUE -> JpegWriter.CompressionFromMetaData
                MimeTypeUtils.IMAGE_PNG_VALUE -> PngWriter.NoCompression
                else -> throw IllegalArgumentException("Unsupported content type: $contentType")
            }
        return invertedImage.bytes(writer)
    }
}
