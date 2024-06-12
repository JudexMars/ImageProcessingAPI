package org.judexmars.imagecrud.dto.auth;

/**
 * Simple DTO for storing access and refresh tokens.
 *
 * @param accessToken token that is used for authorizing user
 * @param refreshToken token that is used for getting new access token
 */
public record TokensHolder(
    String accessToken,
    String refreshToken
) {
}
