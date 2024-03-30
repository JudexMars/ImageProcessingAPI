package org.judexmars.imagecrud.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "JwtResponse")
public record JwtResponseDto(
        String accountId,
        String username,
        String accessToken,
        String refreshToken
) {
}
