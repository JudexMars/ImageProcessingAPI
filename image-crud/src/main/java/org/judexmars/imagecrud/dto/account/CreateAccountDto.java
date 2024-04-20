package org.judexmars.imagecrud.dto.account;

import jakarta.validation.constraints.NotBlank;

/**
 * Create account DTO request.
 *
 * @param username        username of the account
 * @param password        password of the account
 * @param confirmPassword confirm password of the account
 */
public record CreateAccountDto(
    @NotBlank
    String username,
    @NotBlank
    String password,
    @NotBlank
    String confirmPassword
) {
}
