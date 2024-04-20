package org.judexmars.imagecrud.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * JWT request DTO.
 *
 * @param username username of the account
 * @param password password of the account
 */
@Schema(name = "JwtRequest")
public record JwtRequestDto(
        @NotBlank
        String username,
        @NotBlank
        String password
) {
}
