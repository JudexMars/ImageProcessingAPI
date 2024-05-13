package org.judexmars.imageprocessor.unit

import com.sksamuel.scrimage.ImmutableImage
import org.judexmars.imageprocessor.config.ProcessorProperties
import org.judexmars.imageprocessor.dto.ImageStatusMessage
import org.judexmars.imageprocessor.dto.S3ImageDto
import org.judexmars.imageprocessor.service.CropProcessor
import org.judexmars.imageprocessor.service.S3Service
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.kafka.core.KafkaTemplate
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class CropProcessorTest {
    private val s3Service = mock<S3Service>()
    private val kafkaTemplate = mock<KafkaTemplate<String, ImageStatusMessage>>()
    private val properties =
        ProcessorProperties(
            type = "CROP",
            wipTopic = "wip",
            doneTopic = "done",
            concurrency = 2,
            group = "test-group",
        )

    private val cropProcessor = spy(CropProcessor(s3Service, kafkaTemplate, properties))

    @Test
    @DisplayName("Test crop process")
    fun testCropProcess() {
        // Set up
        val imageId = UUID.randomUUID()
        val requestId = UUID.randomUUID()
        val link = UUID.randomUUID()
        val sourceImage =
            S3ImageDto(
                link = link.toString(),
                bytes = byteArrayOf(),
                contentType = "image/jpeg",
                size = 0,
            )
        val processedImageBytes = byteArrayOf(1, 2, 3, 4) // Example processed image
        val props = mapOf("x1" to "100", "y1" to "100", "x2" to "200", "y2" to "200")
        val filters = listOf("CROP", "OTHER_FILTER")
        val message =
            ImageStatusMessage(
                imageId = imageId,
                filters = filters,
                props = props,
                requestId = requestId,
            )

        whenever(s3Service.downloadImage(any(), any())).thenReturn(sourceImage)
        whenever(s3Service.uploadImage(any(), any(), any(), any())).thenReturn(sourceImage)
        doReturn(processedImageBytes).whenever(cropProcessor).crop(any(), any(), any(), any(), any(), any())

        // Act
        cropProcessor.process(message)

        // Verify
        verify(s3Service).downloadImage(eq(imageId.toString()), eq(true))
        verify(s3Service).uploadImage(
            eq(processedImageBytes),
            eq("image/jpeg"),
            eq(processedImageBytes.size.toLong()),
            eq(true),
        )
        verify(kafkaTemplate).send(eq(properties.wipTopic), any())
    }

    @Test
    @DisplayName("Test that crop process skips message which has no required parameters")
    fun testCropSkipWithNoRequiredParameters() {
        // Set up
        val imageId = UUID.randomUUID()
        val requestId = UUID.randomUUID()
        val props = emptyMap<String, String>()
        val filters = listOf("CROP")
        val message =
            ImageStatusMessage(
                imageId = imageId,
                filters = filters,
                props = props,
                requestId = requestId,
            )

        // Act
        cropProcessor.process(message)

        // Verify
        verify(s3Service, never()).downloadImage(any(), any())
    }

    @Test
    @DisplayName("Test crop function with jpeg image")
    fun testCropJpeg() {
        val sourceImage = createTestJpegImage()
        val x1 = 50
        val y1 = 50
        val x2 = 150
        val y2 = 150

        val croppedBytes = cropProcessor.crop(sourceImage, "image/jpeg", x1, y1, x2, y2)
        val croppedImage = ImmutableImage.loader().fromBytes(croppedBytes)

        // Check dimensions of the cropped image
        assert(croppedImage.width == x2 - x1 && croppedImage.height == y2 - y1)
    }

    @Test
    @DisplayName("Test crop function with png image")
    fun testCropPng() {
        val sourceImage = createTestJpegImage()
        val x1 = 10
        val y1 = 10
        val x2 = 100
        val y2 = 100

        val croppedBytes = cropProcessor.crop(sourceImage, "image/png", x1, y1, x2, y2)
        val croppedImage = ImmutableImage.loader().fromBytes(croppedBytes)

        // Check dimensions of the cropped image
        assert(croppedImage.width == x2 - x1 && croppedImage.height == y2 - y1)
    }

    @Test
    @DisplayName("Test crop with image of a wrong type")
    fun testCropWrongContentType() {
        val sourceImage = createTestJpegImage()
        val x1 = 10
        val y1 = 10
        val x2 = 100
        val y2 = 100

        // Expect an IllegalArgumentException for unsupported content types
        assertThrows(IllegalArgumentException::class.java) {
            cropProcessor.crop(sourceImage, "image/gif", x1, y1, x2, y2)
        }
    }
}
