package org.judexmars.imagecrud.unit.service

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.judexmars.imagecrud.dto.image.ImageDto
import org.judexmars.imagecrud.dto.image.ImageResponseDto
import org.judexmars.imagecrud.exception.DeleteFileException
import org.judexmars.imagecrud.exception.ImageNotFoundException
import org.judexmars.imagecrud.exception.UploadFailedException
import org.judexmars.imagecrud.mapper.ImageMapper
import org.judexmars.imagecrud.model.AccountEntity
import org.judexmars.imagecrud.model.ImageEntity
import org.judexmars.imagecrud.repository.ImageRepository
import org.judexmars.imagecrud.service.AccountService
import org.judexmars.imagecrud.service.ImageService
import org.judexmars.imagecrud.service.S3Service
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.*
import org.springframework.mock.web.MockMultipartFile
import java.util.*

internal class ImageServiceTest {

    private val minioService: S3Service = mock()
    private val accountService: AccountService = mock()
    private val imageRepository: ImageRepository = mock()
    private val mapper: ImageMapper = mock()

    private val imageService = spy(ImageService(imageRepository, mapper, minioService, accountService))

    @Test
    @DisplayName("Save image")
    fun saveImage() {
        // Given
        val uuid = UUID.randomUUID()
        val mockMultipartFile = MockMultipartFile(
            "file",
            "test.jpg", "image/jpeg", "test image".toByteArray()
        )
        val username = "testUser"
        val mockedImage = ImageDto("test.jpg", 10, "550e8400-e29b-41d4-a716-44665544000")
        val mockedAccount = AccountEntity()
        val mockedImageEntity = ImageEntity().setId(uuid)

        whenever(minioService.uploadImage(any())).thenReturn(mockedImage)
        whenever(accountService.getEntityByUsername(username)).thenReturn(mockedAccount)
        whenever(mapper.toImageEntity(mockedImage)).thenReturn(mockedImageEntity)
        whenever(imageRepository.save(mockedImageEntity)).thenReturn(mockedImageEntity)

        // When
        val responseDto = imageService.uploadImage(mockMultipartFile, username)

        // Then
        assertNotNull(responseDto)
        assertEquals(uuid.toString(), responseDto.imageId)
    }

    @Test
    @DisplayName("Fail save image")
    fun saveImageFail() {
        // Given
        val mockMultipartFile = MockMultipartFile(
            "file",
            "test.jpg", "image/jpeg", "test image".toByteArray()
        )
        val username = "testUser"

        // When
        whenever(minioService.uploadImage(any())).thenThrow(RuntimeException())

        // Then
        assertThrows<UploadFailedException> {
            imageService.uploadImage(mockMultipartFile, username)
        }
    }

    @Test
    @DisplayName("Get image meta-info")
    fun getImage() {
        // Given
        val id = UUID.randomUUID()
        val accountEntity = AccountEntity().setId(UUID.randomUUID())
        val mockedEntity = ImageEntity().setId(id).setAuthor(accountEntity)
        val expectedDto = ImageDto("filename", 10, "link")

        whenever(imageRepository.findById(id)).thenReturn(Optional.of(mockedEntity))
        whenever(mapper.toImageDto(mockedEntity)).thenReturn(expectedDto)

        // When
        val resultDto = imageService.getImageMeta(id, accountEntity.id)

        // Then
        assertEquals(expectedDto.filename, resultDto.filename)
    }

    @Test
    @DisplayName("Delete image")
    fun deleteImage() {
        // Given
        val id = UUID.randomUUID()
        val mockedImage = ImageDto("filename", 10, "link")

        doReturn(mockedImage).whenever(imageService).getImageMeta(id, null)

        // When
        imageService.deleteImage(id, null)

        // Then
        verify(imageRepository, times(1)).deleteById(id)
        verify(minioService, times(1)).deleteImage(mockedImage.link)
    }

    @Test
    @DisplayName("Delete image which doesn't exist")
    fun deleteImageImageNotFoundException() {
        // Given
        val id = UUID.randomUUID()

        doThrow(ImageNotFoundException("link")).whenever(imageService).getImageMeta(id, null)

        // When & then
        assertThrows<ImageNotFoundException> {
            imageService.deleteImage(id, null)
        }
        verify(imageRepository, never()).deleteById(id)
        verify(minioService, never()).deleteImage(anyString())
    }

    @Test
    @DisplayName("Delete image which can't be found in S3")
    fun deleteImageDeleteException() {
        // Given
        val id = UUID.randomUUID()
        val mockedImage = ImageDto("filename", 1, "link")

        doReturn(mockedImage).whenever(imageService).getImageMeta(id, null)
        whenever(minioService.deleteImage(anyString())).thenThrow(DeleteFileException())

        // When & then
        assertThrows<DeleteFileException> {
            imageService.deleteImage(id, null)
        }
        verify(imageRepository, times(1)).deleteById(id)
        verify(minioService, times(1)).deleteImage(mockedImage.link)
    }

    @Test
    @DisplayName("Get images of user")
    fun getImagesOfUser() {
        // Given
        val accountId = UUID.randomUUID()
        val mockedImageEntities = listOf(
            ImageEntity().setId(UUID.randomUUID()).setFilename("1").setSize(10),
            ImageEntity().setId(UUID.randomUUID()).setFilename("2").setSize(10)
        )
        val expectedDtos = listOf(
            ImageResponseDto(mockedImageEntities[0].id.toString(), "1", 10),
            ImageResponseDto(mockedImageEntities[1].id.toString(), "2", 10)
        )

        whenever(imageRepository.findByAuthorId(accountId)).thenReturn(mockedImageEntities)
        whenever(mapper.toImageResponseDto(mockedImageEntities[0])).thenReturn(expectedDtos[0])
        whenever(mapper.toImageResponseDto(mockedImageEntities[1])).thenReturn(expectedDtos[1])

        // When
        val resultDtos = imageService.getImagesOfUser(accountId)

        // Then
        assertEquals(expectedDtos.size, resultDtos.size)
        assertEquals(expectedDtos[0].imageId, resultDtos[0].imageId)
        assertEquals(expectedDtos[1].imageId, resultDtos[1].imageId)
    }

    @Test
    fun getImagesOfUserEmpty() {
        // Given
        val accountId = UUID.randomUUID()
        whenever(imageRepository.findByAuthorId(accountId)).thenReturn(emptyList())

        // When
        val resultDtos = imageService.getImagesOfUser(accountId)

        // Then
        assertEquals(0, resultDtos.size)
    }
}