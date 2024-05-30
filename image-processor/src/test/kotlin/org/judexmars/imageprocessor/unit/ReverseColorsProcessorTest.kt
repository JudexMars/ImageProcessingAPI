package org.judexmars.imageprocessor.unit

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.color.RGBColor
import org.judexmars.imageprocessor.config.ProcessorProperties
import org.judexmars.imageprocessor.dto.ImageStatusMessage
import org.judexmars.imageprocessor.dto.S3ImageDto
import org.judexmars.imageprocessor.service.ReverseColorsProcessor
import org.judexmars.imageprocessor.service.S3Service
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.kafka.core.KafkaTemplate
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ReverseColorsProcessorTest {
    private val s3Service = mock<S3Service>()
    private val kafkaTemplate = mock<KafkaTemplate<String, ImageStatusMessage>>()
    private val properties =
        ProcessorProperties(
            type = "REVERSE_COLORS",
            wipTopic = "wip",
            doneTopic = "done",
            concurrency = 2,
            group = "test-group",
        )

    private val reverseColorsProcessor = spy(ReverseColorsProcessor(s3Service, kafkaTemplate, properties))

    @Test
    @DisplayName("Test reverse colors processor")
    fun `test process with reverse color operation`() {
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
        val filters = listOf("REVERSE_COLORS", "OTHER_FILTER")
        val message =
            ImageStatusMessage(
                imageId = imageId,
                filters = filters,
                props = emptyMap(),
                requestId = requestId,
            )

        whenever(s3Service.downloadImage(any(), any())).thenReturn(sourceImage)
        whenever(s3Service.uploadImage(any(), any(), any(), any())).thenReturn(sourceImage)
        doReturn(processedImageBytes).whenever(reverseColorsProcessor).applyFilter(
            any(),
            any(),
            eq(null),
        )

        // Act
        reverseColorsProcessor.process(message)

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
    @DisplayName("Test that reverse color processor skips unrelated filters")
    fun testSkippingUnrelatedFilters() {
        // Set up
        val imageId = UUID.randomUUID()
        val filters = listOf("SOME_OTHER_FILTER")
        val message =
            ImageStatusMessage(
                imageId = imageId,
                filters = filters,
                requestId = UUID.randomUUID(),
                props = emptyMap(),
            )

        // Act
        reverseColorsProcessor.process(message)

        // Verify
        verify(s3Service, never()).downloadImage(any(), any())
        verify(kafkaTemplate, never()).send(any(), any())
    }

    @Test
    @DisplayName("Test that colors are correctly inverted")
    fun testColorInversion() {
        val sourceImageBytes = createSimpleColorTestPngImage()
        val invertedBytes = reverseColorsProcessor.applyFilter(sourceImageBytes, "image/png")
        val invertedImage = ImmutableImage.loader().fromBytes(invertedBytes)

        val expectedColors =
            arrayOf(
                RGBColor(255, 255, 0),
                RGBColor(255, 0, 255),
                RGBColor(0, 255, 255),
                RGBColor(0, 0, 0),
            )

        assertTrue {
            arrayOf(
                invertedImage.pixel(0, 0).toColor(),
                invertedImage.pixel(1, 0).toColor(),
                invertedImage.pixel(2, 0).toColor(),
                invertedImage.pixel(3, 0).toColor(),
            ).contentEquals(expectedColors)
        }
    }
}
