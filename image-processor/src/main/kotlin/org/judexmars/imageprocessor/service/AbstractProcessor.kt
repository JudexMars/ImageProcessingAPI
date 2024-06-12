package org.judexmars.imageprocessor.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.judexmars.imageprocessor.config.ProcessorProperties
import org.judexmars.imageprocessor.dto.ImageStatusMessage
import org.judexmars.imageprocessor.dto.S3ImageDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import java.util.UUID

abstract class AbstractProcessor<T>(
    private val s3Service: S3Service,
    private val template: KafkaTemplate<String, ImageStatusMessage>,
    private val properties: ProcessorProperties,
    private val propsType: Class<T>? = null,
    private val objectMapper: ObjectMapper? = null,
) : Processor {
    private companion object {
        val log: Logger = LoggerFactory.getLogger(AbstractProcessor::class.java)
    }

    override fun process(message: ImageStatusMessage) {
        log.info("${properties.type} processor started processing new message: $message")
        val filters = message.filters
        val props = message.props
        if (filters.isEmpty() || filters.first() != properties.type) return

        var nextImageId: UUID = message.imageId
        val nextFilters = filters.drop(1)
        val isIntermediate = nextFilters.isNotEmpty()

        try {
            var propsDto: T? = null
            if (propsType != null && objectMapper != null) {
                propsDto = objectMapper.convertValue(message.props[properties.type], propsType)
                if (propsDto == null) throw IllegalArgumentException("Required properties are not provided")
            }

            log.info("Before download")
            val source: S3ImageDto =
                try {
                    s3Service.downloadImage(message.imageId.toString(), true)
                } catch (e: Exception) {
                    s3Service.downloadImage(message.imageId.toString(), false)
                }

            log.info("Before modified image")
            val modifiedImage =
                applyFilter(
                    source.bytes,
                    source.contentType,
                    propsDto,
                )
            log.info("After modified image")

            val modifiedS3Image =
                s3Service.uploadImage(
                    modifiedImage,
                    source.contentType,
                    modifiedImage.size.toLong(),
                    isIntermediate,
                )
            nextImageId = UUID.fromString(modifiedS3Image.link)
        } catch (e: Exception) {
            log.info("Filter processing wasn't successful:\n${e.message}")
        } finally {
            val nextProps = props.filterKeys { properties.type != it }
            val nextTopic = if (isIntermediate) properties.wipTopic else properties.doneTopic

            val nextMessage =
                message.copy(
                    imageId = nextImageId,
                    filters = nextFilters,
                    props = nextProps,
                )
            template.send(nextTopic, nextMessage)
        }
    }

    abstract fun applyFilter(
        source: ByteArray,
        contentType: String,
        props: T? = null,
    ): ByteArray
}
