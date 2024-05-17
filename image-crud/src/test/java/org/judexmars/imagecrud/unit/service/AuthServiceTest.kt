package org.judexmars.imagecrud.unit.service

import org.judexmars.imagecrud.exception.InvalidJwtException
import org.judexmars.imagecrud.model.AccountEntity
import org.judexmars.imagecrud.service.AccountService
import org.judexmars.imagecrud.service.AuthService
import org.judexmars.imagecrud.service.JwtTokenService
import org.judexmars.imagecrud.service.RedisTokenService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.AuthenticationManager
import java.util.UUID

internal class AuthServiceTest {
    private val authenticationManager: AuthenticationManager = mock()
    private val jwtTokenService: JwtTokenService = mock()
    private val redisTokenService: RedisTokenService = mock()
    private val accountService: AccountService = mock()

    private val authService =
        AuthService(jwtTokenService, authenticationManager, accountService, redisTokenService)

    @Test
    @DisplayName("Create auth tokens")
    fun createAuthTokens() {
        // Given
        val username = "testUser"
        val password = "testPassword"
        val accessToken = "testAccessToken"
        val refreshToken = "testRefreshToken"
        val account = AccountEntity()

        whenever(jwtTokenService.generateAccessToken(account)).thenReturn(accessToken)
        whenever(jwtTokenService.generateRefreshToken(account)).thenReturn(refreshToken)

        // When
        val tokens = authService.createAuthTokens(account, username, password)

        // Then
        assertEquals(accessToken, tokens.accessToken)
        assertEquals(refreshToken, tokens.refreshToken)

        verify(authenticationManager).authenticate(any()) // Verify authenticationManager.authenticate is called with any parameters
        verify(redisTokenService).saveRefreshToken(
            username,
            refreshToken,
        ) // Verify redisTokenService.saveRefreshToken is called with expected parameters
    }

    @Test
    @DisplayName("Refresh token")
    fun refresh() {
        // Given
        val refreshToken = "testRefreshToken"
        val accessToken = "testAccessToken"
        val accountId = UUID.randomUUID()
        val username = "testUser"
        val userDetails = AccountEntity().setId(accountId).setUsername(username)

        whenever(jwtTokenService.getUsernameFromRefreshToken(refreshToken)).thenReturn(username)
        whenever(redisTokenService.deleteRefreshToken(username, refreshToken)).thenReturn(true)
        whenever(accountService.loadUserByUsername(username)).thenReturn(userDetails)
        whenever(jwtTokenService.generateAccessToken(userDetails)).thenReturn(accessToken)
        whenever(jwtTokenService.generateRefreshToken(userDetails)).thenReturn(refreshToken)

        // When
        val result = authService.refresh(refreshToken)

        // Then
        assertNotNull(result)
        assertEquals(accessToken, result.tokens.accessToken)
        assertEquals(refreshToken, result.tokens.refreshToken)
        assertEquals(accountId.toString(), result.accountId)
        assertEquals(username, result.username)

        verify(jwtTokenService).getUsernameFromRefreshToken(refreshToken)
        verify(redisTokenService).deleteRefreshToken(username, refreshToken)
        verify(accountService).loadUserByUsername(username)
        verify(jwtTokenService).generateAccessToken(userDetails)
        verify(redisTokenService).saveRefreshToken(username, refreshToken)
    }

    @Test
    @DisplayName("Invalid refresh token")
    fun refreshInvalid() {
        // Given
        val refreshToken = "invalidToken"

        whenever(jwtTokenService.getUsernameFromRefreshToken(refreshToken)).thenThrow(InvalidJwtException())

        // When & then
        assertThrows<InvalidJwtException> {
            authService.refresh(refreshToken)
        }

        verify(jwtTokenService).getUsernameFromRefreshToken(refreshToken)
        verifyNoInteractions(redisTokenService, accountService)
    }
}
