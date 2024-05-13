package org.judexmars.imagecrud.unit.service

import io.minio.MinioClient
import org.judexmars.imagecrud.config.minio.MinioProperties
import org.judexmars.imagecrud.exception.UploadFailedException
import org.judexmars.imagecrud.service.S3Service
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.web.multipart.MultipartFile
import kotlin.test.assertFailsWith

internal class S3ServiceTest {

    private val minioClient: MinioClient = mock()
    private val minioProperties: MinioProperties = mock()

    private val minioService = S3Service(minioClient, minioProperties)

    @Test
    fun init() {
        // Given
        whenever(minioProperties.bucket).thenReturn("test-bucket")
        whenever(minioClient.bucketExists(any())).thenReturn(true)

        // When
        minioService.init()

        // Then
        verify(minioClient, never()).makeBucket(any())
    }

    @Test
    fun initBucketNotFound() {
        // Given
        whenever(minioProperties.bucket).thenReturn("test-bucket")
        whenever(minioClient.bucketExists(any())).thenReturn(false)

        // When
        minioService.init()

        // Then
        verify(minioClient).makeBucket(any())
    }

    @Test
    fun uploadImage() {
        // Given
        val file = mock(MultipartFile::class.java)
        val expectedFilename = "testImage.png"
        val content = "testFileContent".toByteArray()

        whenever(minioProperties.bucket).thenReturn("test-bucket")
        whenever(minioProperties.imageSize).thenReturn(10485760)
        whenever(file.contentType).thenReturn("image/png")
        whenever(file.bytes).thenReturn(content)
        whenever(file.originalFilename).thenReturn("testImage.png")
        whenever(file.size).thenReturn(content.size.toLong())

        // When
        val result = minioService.uploadImage(file)

        // Then
        assertEquals("testImage.png", result.filename)
        assertEquals("testFileContent".toByteArray().size, result.size)
        assertEquals(expectedFilename, result.filename)
    }

    @Test
    fun uploadImageInvalidFileType() {
        // Given
        val file = mock(MultipartFile::class.java)

        whenever(file.contentType).thenReturn("text/plain")

        // When & then
        assertFailsWith<UploadFailedException> {
            minioService.uploadImage(file)
        }
    }

    @Test
    fun uploadImageInvalidFileSize() {
        // Given
        val file = mock(MultipartFile::class.java)

        whenever(file.contentType).thenReturn("image/png")
        whenever(file.size).thenReturn(999999999999999999)

        // When & then
        assertFailsWith<UploadFailedException> {
            minioService.uploadImage(file)
        }
    }
}