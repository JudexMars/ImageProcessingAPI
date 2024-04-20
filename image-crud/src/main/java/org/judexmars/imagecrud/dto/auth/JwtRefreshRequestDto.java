package org.judexmars.imagecrud.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for refreshing JWT token.
 *
 * @param token JWT token to be refreshed
 */
@Schema(name = "JwtRefreshRequest")
public record JwtRefreshRequestDto(
    @NotBlank
    String token
) {
}
