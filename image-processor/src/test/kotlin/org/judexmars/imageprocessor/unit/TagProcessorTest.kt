package org.judexmars.imageprocessor.unit

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.judexmars.imageprocessor.config.ImaggaProperties
import org.judexmars.imageprocessor.config.ProcessorProperties
import org.judexmars.imageprocessor.dto.ImageStatusMessage
import org.judexmars.imageprocessor.dto.imagga.Status
import org.judexmars.imageprocessor.dto.imagga.tags.Tag
import org.judexmars.imageprocessor.dto.imagga.tags.TagDetail
import org.judexmars.imageprocessor.dto.imagga.tags.TagsResponse
import org.judexmars.imageprocessor.dto.imagga.tags.TagsResult
import org.judexmars.imageprocessor.dto.imagga.upload.UploadResponse
import org.judexmars.imageprocessor.dto.imagga.upload.UploadResult
import org.judexmars.imageprocessor.dto.imagga.usage.UsageResponse
import org.judexmars.imageprocessor.dto.imagga.usage.UsageResult
import org.judexmars.imageprocessor.service.S3Service
import org.judexmars.imageprocessor.service.TagProcessor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient

@ExtendWith(MockitoExtension::class)
class TagProcessorTest {
    private val s3Service: S3Service = mock()

    private val kafkaTemplate: KafkaTemplate<String, ImageStatusMessage> = mock()

    private val processorProperties =
        ProcessorProperties(
            type = "TAG",
            wipTopic = "wip",
            doneTopic = "done",
            concurrency = 2,
            group = "test-group",
        )

    private val imaggaProperties: ImaggaProperties = ImaggaProperties()

    private val restClientBuilder = RestClient.builder()

    private val restClient by lazy { restClientBuilder.build() }

    private val mockServer: MockRestServiceServer = MockRestServiceServer.bindTo(restClientBuilder).build()

    private val tagProcessor: TagProcessor =
        TagProcessor(
            s3Service,
            kafkaTemplate,
            processorProperties,
            imaggaProperties,
            restClient,
        )

    private val objectMapper = jacksonObjectMapper()

    @AfterEach
    fun setUp() {
        mockServer.reset()
    }

    @Test
    fun `applyFilter should throw IllegalStateException when counter is zero or less`() {
        // Mock the remainingRequests AtomicInteger to return 0
        val usageResponse =
            objectMapper.writeValueAsBytes(
                UsageResponse(
                    result =
                        UsageResult(
                            monthlyLimit = 2000,
                            monthlyProcessed = 2000,
                        ),
                    status = Status("200", "OK"),
                ),
            )

        mockServer.expect(requestTo(imaggaProperties.uri + "/usage"))
            .andRespond(withSuccess(usageResponse, MediaType.APPLICATION_JSON))

        // Prepare test data
        val source = byteArrayOf()
        val contentType = "image/jpeg"

        // Execute and verify
        assertThrows(IllegalStateException::class.java) { tagProcessor.applyFilter(source, contentType) }
    }

    @Test
    fun `applyFilter should execute successfully`() {
        val usageResponse =
            objectMapper.writeValueAsBytes(
                UsageResponse(
                    result =
                        UsageResult(
                            monthlyLimit = 2000,
                            monthlyProcessed = 1000,
                        ),
                    status = Status("200", "OK"),
                ),
            )

        mockServer
            .expect(requestTo(imaggaProperties.uri + "/usage"))
            .andRespond(withSuccess(usageResponse, MediaType.APPLICATION_JSON))

        val uploadId = "testUploadId"

        val uploadResponse =
            objectMapper.writeValueAsBytes(
                UploadResponse(
                    result =
                        UploadResult(
                            uploadId = uploadId,
                        ),
                    status = Status("200", "OK"),
                ),
            )

        mockServer
            .expect(requestTo(imaggaProperties.uri + "/uploads"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(uploadResponse, MediaType.APPLICATION_JSON))

        val tagsResponse =
            objectMapper.writeValueAsBytes(
                TagsResponse(
                    TagsResult(
                        listOf(Tag(TagDetail("Default"))),
                    ),
                    Status("200", "OK"),
                ),
            )

        mockServer
            .expect(requestTo(imaggaProperties.uri + "/tags?image_upload_id=$uploadId&limit=3"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(tagsResponse, MediaType.APPLICATION_JSON))

        // Prepare test data
        val source = createTestJpegImage()
        val contentType = "image/jpeg"

        // Execute and verify
        assertNotNull(tagProcessor.applyFilter(source, contentType, null))
    }
}
