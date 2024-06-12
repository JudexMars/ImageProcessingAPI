package org.judexmars.imagecrud.service;

import lombok.RequiredArgsConstructor;
import org.judexmars.imagecrud.dto.auth.JwtResponseDto;
import org.judexmars.imagecrud.dto.auth.TokensHolder;
import org.judexmars.imagecrud.exception.InvalidJwtException;
import org.judexmars.imagecrud.model.AccountEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Authentication Service.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

  private final JwtTokenService jwtTokenService;
  private final AuthenticationManager authenticationManager;
  private final AccountService accountService;
  private final RedisTokenService redisTokenService;

  /**
   * Generate JWT tokens (access and refresh) based on user information.
   *
   * @param userDetails core info of user
   * @param username    entered name
   * @param password    entered password
   * @return {accessToken, refreshToken}
   */
  public TokensHolder createAuthTokens(UserDetails userDetails, String username, String password) {
    authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(username, password));
    var accessToken = jwtTokenService.generateAccessToken(userDetails);
    var refreshToken = jwtTokenService.generateRefreshToken(userDetails);
    redisTokenService.saveRefreshToken(username, refreshToken);
    return new TokensHolder(accessToken, refreshToken);
  }

  /**
   * Generate new JWT tokens (access and refresh) based on provided refresh token.
   *
   * @param refreshToken provided refresh token
   * @return {accessToken, refreshToken, userId, username}
   */
  public JwtResponseDto refresh(String refreshToken) throws InvalidJwtException {
    var username = jwtTokenService.getUsernameFromRefreshToken(refreshToken);
    var deleted = redisTokenService.deleteRefreshToken(username, refreshToken);
    if (deleted) {
      var account = (AccountEntity) accountService.loadUserByUsername(username);
      var accessToken = jwtTokenService.generateAccessToken(account);
      refreshToken = jwtTokenService.generateRefreshToken(account);
      redisTokenService.saveRefreshToken(username, refreshToken);
      return new JwtResponseDto(
          String.valueOf(account.getId()),
          account.getUsername(),
          new TokensHolder(accessToken, refreshToken));
    }
    throw new InvalidJwtException();
  }

  /**
   * Authenticate user by username and password.
   *
   * @param username username
   * @param password password
   * @return DTO containing account's id, username and tokens
   */
  public JwtResponseDto authenticate(String username, String password) {
    var account = (AccountEntity) accountService.loadUserByUsername(username);
    var tokens = createAuthTokens(account, username, password);
    return new JwtResponseDto(account.getId().toString(), username, tokens);
  }
}
