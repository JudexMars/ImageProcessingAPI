package org.judexmars.imagecrud.intergration

import org.judexmars.imagecrud.config.AbstractBaseTest
import org.judexmars.imagecrud.dto.account.CreateAccountDto
import org.judexmars.imagecrud.repository.AccountRepository
import org.judexmars.imagecrud.service.AccountService
import org.judexmars.imagecrud.service.AuthService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
class AuthTest : AbstractBaseTest() {
    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var accountService: AccountService

    @Autowired
    private lateinit var authService: AuthService

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun clear() {
        accountRepository.deleteAll()
    }

    @Test
    @DisplayName("Successful registration")
    fun signup() {
        // Given
        val request =
            """
            {
                "username":"string",
                "password":"12345",
                "confirmPassword":"12345"
            }
            """.trimIndent()

        // When & then
        mockMvc.perform(
            MockMvcRequestBuilders.post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request),
        )
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("Unsuccessful registration because of password confirmation error")
    fun signupConfirmException() {
        // Given
        val request =
            """
            {
                "username":"string",
                "password":"12345",
                "confirmPassword":"2"
            }
            """.trimIndent()

        // When & then
        mockMvc.perform(
            MockMvcRequestBuilders.post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("Login successfully")
    fun login() {
        // Given
        val x = accountService.createAccount(CreateAccountDto("john", "12345", "12345"))
        println(x)
        val request =
            """
            {
                "username":"john",
                "password":"12345"
            }
            """.trimIndent()

        // When & then
        mockMvc.perform(
            MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request),
        )
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("Login with wrong password")
    fun loginWrongPassword() {
        // Given
        accountService.createAccount(CreateAccountDto("john", "12345", "12345"))
        val request =
            """
            {
                "username":"john",
                "password":"such a lust for revenge"
            }
            """.trimIndent()

        // When & then
        mockMvc.perform(
            MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request),
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("Refresh token")
    fun refreshToken() {
        // Given
        accountService.createAccount(CreateAccountDto("john", "12345", "12345"))
        val token =
            authService.createAuthTokens(
                accountService
                    .loadUserByUsername("john"),
                "john",
                "12345",
            ).refreshToken
        val request =
            """
            {
                "token":"$token"
            }
            """.trimIndent()

        // When & then
        mockMvc.perform(
            MockMvcRequestBuilders.post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request),
        )
            .andExpect(status().isOk)
    }
}
