package org.judexmars.imagecrud.unit.service

import org.judexmars.imagecrud.dto.account.AccountDto
import org.judexmars.imagecrud.dto.account.CreateAccountDto
import org.judexmars.imagecrud.exception.AccountAlreadyExistsException
import org.judexmars.imagecrud.exception.AccountNotFoundException
import org.judexmars.imagecrud.exception.ConfirmPasswordException
import org.judexmars.imagecrud.mapper.AccountMapper
import org.judexmars.imagecrud.model.AccountEntity
import org.judexmars.imagecrud.model.RoleEntity
import org.judexmars.imagecrud.repository.AccountRepository
import org.judexmars.imagecrud.service.AccountService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*

internal class AccountServiceTest {

    private val accountRepository: AccountRepository = mock()
    private val passwordEncoder = BCryptPasswordEncoder()
    private val accountMapper: AccountMapper = mock()

    private val accountService = spy(AccountService(accountRepository, passwordEncoder, accountMapper, mock()))

    @Test
    @DisplayName("Create account")
    fun createAccount() {
        // Given
        val createAccountDto = CreateAccountDto("testUser", "password", "password")
        val accountEntity = AccountEntity().setId(UUID.randomUUID()).setUsername("testUser")
            .setPassword("encodedPassword").setEnabled(true).setRoles(emptyList())

        whenever(accountRepository.findByUsername(createAccountDto.username())).thenReturn(Optional.empty())
        whenever(accountMapper.toAccountEntity(createAccountDto)).thenReturn(accountEntity)
        whenever(accountRepository.save(accountEntity)).thenReturn(accountEntity)
        whenever(accountMapper.toAccountDto(accountEntity)).thenReturn(
            AccountDto(
                accountEntity.id,
                accountEntity.username
            )
        )
        doReturn(RoleEntity().setName("ROLE_VIEWER")).whenever(accountService).defaultRole

        // When
        val result = accountService.createAccount(createAccountDto)

        // Then
        assertNotNull(result)
        assertEquals(accountEntity.id, result.id)
        assertEquals(accountEntity.username, result.username)
    }

    @Test
    @DisplayName("Create account with the taken username")
    fun createAccountAccountAlreadyExistsException() {
        // Given
        val createAccountDto = CreateAccountDto("existingUser", "password", "password")
        val existingAccount = AccountEntity().setId(UUID.randomUUID()).setUsername("existingUser")
            .setPassword("encodedPassword").setEnabled(true).setRoles(emptyList())
        whenever(accountRepository.findByUsername(createAccountDto.username())).thenReturn(Optional.of(existingAccount))

        // When & then
        assertThrows<AccountAlreadyExistsException> {
            accountService.createAccount(createAccountDto)
        }
    }

    @Test
    @DisplayName("Create account with wrong confirmation password")
    fun createAccountConfirmPasswordException() {
        // Given
        val createAccountDto = CreateAccountDto("testUser", "password", "differentPassword")

        // When & then
        assertThrows<ConfirmPasswordException> {
            accountService.createAccount(createAccountDto)
        }
    }

    @Test
    @DisplayName("Load user")
    fun loadUserByUsername() {
        // Given
        val username = "testUser"
        val userDetails = AccountEntity().setUsername(username)
        whenever(accountRepository.findByUsername(username)).thenReturn(Optional.of(userDetails))

        // When
        val result = accountService.loadUserByUsername(username)

        // Then
        assertEquals(userDetails, result)
    }

    @Test
    @DisplayName("Load non existing user")
    fun loadUserByUsernameNotFoundException() {
        // Given
        val username = "nonExistingUser"
        whenever(accountRepository.findByUsername(username)).thenReturn(Optional.empty())

        // When & Then
        assertThrows<AccountNotFoundException> {
            accountService.loadUserByUsername(username)
        }
    }

    @Test
    @DisplayName("Get user by their username")
    fun getByUsername() {
        // Given
        val uuid = UUID.randomUUID()
        val username = "testUser"
        val accountEntity = AccountEntity().setId(uuid).setUsername(username).setPassword("password").setEnabled(true)
            .setRoles(emptyList())
        val accountDto = AccountDto(uuid, username)
        doReturn(accountEntity).whenever(accountService).getEntityByUsername(username)
        whenever(accountMapper.toAccountDto(accountEntity)).thenReturn(accountDto)

        // When
        val result = accountService.getByUsername(username)

        // Then
        assertEquals(accountDto, result)
    }
}