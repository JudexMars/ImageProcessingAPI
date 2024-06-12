package org.judexmars.imagecrud.dto.auth;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * JWT response DTO.
 *
 * @param accountId    id of the account
 * @param username     username of the account
 * @param tokens       JWT tokens
 */
@Schema(name = "JwtResponse")
public record JwtResponseDto(
    String accountId,
    String username,
    @JsonUnwrapped
    TokensHolder tokens
) {
}
