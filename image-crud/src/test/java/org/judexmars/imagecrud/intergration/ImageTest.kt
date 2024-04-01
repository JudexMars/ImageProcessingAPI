package org.judexmars.imagecrud.intergration

import org.judexmars.imagecrud.config.AbstractBaseTest
import org.judexmars.imagecrud.dto.account.CreateAccountDto
import org.judexmars.imagecrud.repository.AccountRepository
import org.judexmars.imagecrud.repository.ImageRepository
import org.judexmars.imagecrud.service.AccountService
import org.judexmars.imagecrud.service.AuthService
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImageTest : AbstractBaseTest() {

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var accountService: AccountService

    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var imageRepository: ImageRepository

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private lateinit var mockMvc: MockMvc

    private lateinit var uuid: UUID

    @BeforeAll
    fun start() {
        accountRepository.deleteAll()
        imageRepository.deleteAll()
        val account = accountService.createAccount(CreateAccountDto("johnny", "jabroni", "jabroni"))
        uuid = account.id
    }

    private val token: String by lazy {
        "Bearer " + authService.createAuthTokens(
            accountService
                .loadUserByUsername("johnny"), "johnny", "jabroni"
        )[0]
    }

    @Test
    @DisplayName("Upload & download image")
    fun uploadDownloadImage() {
        // Given
        val file: MockMultipartFile? = javaClass.getResourceAsStream("/test_image.png")?.let {
            MockMultipartFile(
                "file", "test-image.jpg", "image/png",
                it
            )
        }

        // When & then
        file?.let { MockMvcRequestBuilders.multipart("/image").file(it).header("Authorization", token) }?.also {
            mockMvc.perform(
                it
            ).andExpect(status().isOk)
        } ?: run {
            throw RuntimeException()
        }

        val imageId = imageRepository.findByAuthorId(uuid)[0]

        mockMvc.perform(MockMvcRequestBuilders.get("/image/${imageId.id}").header("Authorization", token))
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("Download non existing image")
    fun downloadNotExisting() {
        mockMvc.perform(MockMvcRequestBuilders.get("/image/${UUID.randomUUID()}").header("Authorization", token))
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("Delete image")
    fun deleteImage() {
        // Given
        val file: MockMultipartFile? = javaClass.getResourceAsStream("/test_image.png")?.let {
            MockMultipartFile(
                "file", "test-image.jpg", "image/png",
                it
            )
        }

        // When & then
        file?.let { MockMvcRequestBuilders.multipart("/image").file(it).header("Authorization", token) }?.also {
            mockMvc.perform(
                it
            ).andExpect(status().isOk)
        } ?: run {
            throw RuntimeException()
        }

        val imageId = imageRepository.findByAuthorId(uuid)[0]

        mockMvc.perform(MockMvcRequestBuilders.delete("/image/${imageId.id}").header("Authorization", token))
            .andExpect(status().isOk)
    }
}