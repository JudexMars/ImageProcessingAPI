package org.judexmars.imageprocessor.service

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.ratelimiter.annotation.RateLimiter
import io.github.resilience4j.retry.annotation.Retry
import org.judexmars.imageprocessor.config.ImaggaProperties
import org.judexmars.imageprocessor.config.ProcessorProperties
import org.judexmars.imageprocessor.dto.ImageStatusMessage
import org.judexmars.imageprocessor.dto.imagga.tags.Tag
import org.judexmars.imageprocessor.dto.imagga.tags.TagDetail
import org.judexmars.imageprocessor.dto.imagga.tags.TagsResponse
import org.judexmars.imageprocessor.dto.imagga.upload.UploadResponse
import org.judexmars.imageprocessor.dto.imagga.usage.UsageResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient
import java.awt.Color
import java.awt.Font
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.concurrent.atomic.AtomicInteger
import javax.imageio.ImageIO

@Service
@ConditionalOnProperty(name = ["props.type"], havingValue = "TAG")
class TagProcessor(
    s3Service: S3Service,
    template: KafkaTemplate<String, ImageStatusMessage>,
    properties: ProcessorProperties,
    private val imaggaProperties: ImaggaProperties,
    private val restClient: RestClient,
) : AbstractProcessor<Any>(
        s3Service,
        template,
        properties,
    ) {
    private val authHeader: String by lazy {
        val src = "${imaggaProperties.apiKey}:${imaggaProperties.apiSecret}"
        val encoded = Base64.getEncoder().encodeToString(src.toByteArray(Charsets.UTF_8))
        "Basic $encoded"
    }

    private val remainingRequests: AtomicInteger by lazy {
        val response =
            restClient
                .get()
                .uri(imaggaProperties.uri + "/usage")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .body(UsageResponse::class.java)
        if (response?.result == null) {
            AtomicInteger(2000)
        } else {
            AtomicInteger(
                response.result.monthlyLimit -
                    response.result.monthlyProcessed,
            )
        }
    }

    @Retry(name = "imaggaRetry")
    @CircuitBreaker(name = "imaggaCB")
    @RateLimiter(name = "imaggaRL")
    override fun applyFilter(
        source: ByteArray,
        contentType: String,
        props: Any?,
    ): ByteArray {
        if (remainingRequests.get() <= 1) throw IllegalStateException("API Limit Exceed")
        val uploadId = uploadImage(source)
        remainingRequests.decrementAndGet()
        val tags = getTagsForImage(uploadId)
        remainingRequests.decrementAndGet()
        return addTagsToImage(source, contentType, tags)
    }

    private fun uploadImage(source: ByteArray): String {
        val encodedSource = Base64.getEncoder().encode(source)
        val data = LinkedMultiValueMap<String, Any>()
        data.add("image_base64", encodedSource)
        val response =
            restClient
                .post()
                .uri(imaggaProperties.uri + "/uploads")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header(
                    HttpHeaders.AUTHORIZATION,
                    authHeader,
                )
                .body(data)
                .retrieve()
                .body(UploadResponse::class.java)
        response ?: throw HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)
        if (response.result == null) {
            val code = HttpStatus.valueOf(response.status.type.toInt())
            when {
                code.is4xxClientError -> throw HttpClientErrorException(code, response.status.text)
                code.is5xxServerError -> throw HttpServerErrorException(code, response.status.text)
                else -> throw HttpServerErrorException(code, response.status.text)
            }
        }
        return response.result.uploadId
    }

    private fun getTagsForImage(uploadId: String): List<String> {
        val response =
            restClient
                .get()
                .uri(imaggaProperties.uri + "/tags?image_upload_id=$uploadId&limit=3")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .body(TagsResponse::class.java)
        response ?: throw HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)
        val tags =
            response.result.tags
                .map(Tag::tag)
                .map(TagDetail::name)
        return tags.take(3)
    }

    private fun addTagsToImage(
        source: ByteArray,
        contentType: String,
        tags: List<String>,
    ): ByteArray {
        val image = ImageIO.read(ByteArrayInputStream(source))
        val graphics = image.createGraphics()

        val font = Font("Arial", Font.BOLD, image.height / 15)
        graphics.font = font
        graphics.color = Color.BLUE
        graphics.drawString(tags.joinToString(", "), 20, image.height / 10)
        graphics.dispose()

        val output = ByteArrayOutputStream()
        ImageIO.write(image, contentType.removePrefix("image/"), output)
        return output.toByteArray()
    }
}
