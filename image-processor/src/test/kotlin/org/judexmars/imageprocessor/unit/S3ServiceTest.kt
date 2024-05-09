package org.judexmars.imageprocessor.unit

import io.minio.MinioClient
import org.judexmars.imageprocessor.config.minio.MinioProperties
import org.judexmars.imageprocessor.service.S3Service
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.util.UUID

internal class S3ServiceTest {
    private val minioClient: MinioClient = mock()
    private val minioProperties: MinioProperties =
        MinioProperties(
            "x",
            "x",
            "x",
            "test-bucket",
            "test-bucket",
            10485760,
        )

    private val minioService = S3Service(minioClient, minioProperties)

    @Test
    fun uploadImage() {
        // Given
        val content = "testFileContent".toByteArray()

        whenever(minioClient.uploadObject(any())).thenReturn(mock())

        // When
        val result =
            minioService.uploadImage(
                content,
                "image/png",
                content.size.toLong(),
                true,
            )

        // Then
        assertDoesNotThrow { UUID.fromString(result.link) }
        assertEquals(content.size.toLong(), result.size)
    }
}
