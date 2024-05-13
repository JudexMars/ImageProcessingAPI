package org.judexmars.imagecrud.unit.service

import org.judexmars.imagecrud.exception.InvalidJwtException
import org.judexmars.imagecrud.model.AccountEntity
import org.judexmars.imagecrud.service.AccountService
import org.judexmars.imagecrud.service.AuthService
import org.judexmars.imagecrud.service.RedisTokenService
import org.judexmars.imagecrud.utils.JwtTokenUtils
import org.junit.Assert.*
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.AuthenticationManager
import java.util.*

internal class AuthServiceTest {

    private val authenticationManager: AuthenticationManager = mock()
    private val jwtTokenUtils: JwtTokenUtils = mock()
    private val redisTokenService: RedisTokenService = mock()
    private val accountService: AccountService = mock()

    private val authenticationService =
        AuthService(jwtTokenUtils, authenticationManager, accountService, redisTokenService)

    @Test
    @DisplayName("Create auth tokens")
    fun createAuthTokens() {
        // Given
        val username = "testUser"
        val password = "testPassword"
        val accessToken = "testAccessToken"
        val refreshToken = "testRefreshToken"
        val account = AccountEntity()

        whenever(jwtTokenUtils.generateAccessToken(account)).thenReturn(accessToken)
        whenever(jwtTokenUtils.generateRefreshToken(account)).thenReturn(refreshToken)

        // When
        val tokens = authenticationService.createAuthTokens(account, username, password)

        // Then
        assertArrayEquals(arrayOf(accessToken, refreshToken), tokens)

        verify(authenticationManager).authenticate(any()) // Verify authenticationManager.authenticate is called with any parameters
        verify(redisTokenService).saveRefreshToken(
            username,
            refreshToken
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

        whenever(jwtTokenUtils.getUsernameFromRefreshToken(refreshToken)).thenReturn(username)
        whenever(redisTokenService.deleteRefreshToken(username, refreshToken)).thenReturn(true)
        whenever(accountService.loadUserByUsername(username)).thenReturn(userDetails)
        whenever(jwtTokenUtils.generateAccessToken(userDetails)).thenReturn(accessToken)
        whenever(jwtTokenUtils.generateRefreshToken(userDetails)).thenReturn(refreshToken)

        // When
        val result = authenticationService.refresh(refreshToken)

        // Then
        assertNotNull(result)
        assertEquals(accessToken, result[0])
        assertEquals(refreshToken, result[1])
        assertEquals(accountId.toString(), result[2])
        assertEquals(username, result[3])

        verify(jwtTokenUtils).getUsernameFromRefreshToken(refreshToken)
        verify(redisTokenService).deleteRefreshToken(username, refreshToken)
        verify(accountService).loadUserByUsername(username)
        verify(jwtTokenUtils).generateAccessToken(userDetails)
        verify(redisTokenService).saveRefreshToken(username, refreshToken)
    }

    @Test
    @DisplayName("Invalid refresh token")
    fun refreshInvalid() {
        // Given
        val refreshToken = "invalidToken"

        whenever(jwtTokenUtils.getUsernameFromRefreshToken(refreshToken)).thenThrow(InvalidJwtException())

        // When & then
        assertThrows<InvalidJwtException> {
            authenticationService.refresh(refreshToken)
        }

        verify(jwtTokenUtils).getUsernameFromRefreshToken(refreshToken)
        verifyNoInteractions(redisTokenService, accountService)
    }
}