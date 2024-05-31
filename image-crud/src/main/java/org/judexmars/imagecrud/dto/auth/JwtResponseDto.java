package org.judexmars.imagecrud.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * JWT response DTO.
 *
 * @param accountId    id of the account
 * @param username     username of the account
 * @param accessToken  JWT access token
 * @param refreshToken JWT refresh token
 */
@Schema(name = "JwtResponse")
public record JwtResponseDto(
    String accountId,
    String username,
    String accessToken,
    String refreshToken
) {
}
