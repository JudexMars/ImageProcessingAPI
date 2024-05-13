package org.judexmars.imagecrud.service;

import lombok.RequiredArgsConstructor;
import org.judexmars.imagecrud.exception.InvalidJwtException;
import org.judexmars.imagecrud.model.AccountEntity;
import org.judexmars.imagecrud.utils.JwtTokenUtils;
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

  private final JwtTokenUtils jwtTokenUtils;
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
  public String[] createAuthTokens(UserDetails userDetails, String username, String password) {
    authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(username, password));
    var accessToken = jwtTokenUtils.generateAccessToken(userDetails);
    var refreshToken = jwtTokenUtils.generateRefreshToken(userDetails);
    redisTokenService.saveRefreshToken(username, refreshToken);
    return new String[] {accessToken, refreshToken};
  }

  /**
   * Generate new JWT tokens (access and refresh) based on provided refresh token.
   *
   * @param refreshToken provided refresh token
   * @return {accessToken, refreshToken, userId, username}
   */
  public String[] refresh(String refreshToken) throws InvalidJwtException {
    var username = jwtTokenUtils.getUsernameFromRefreshToken(refreshToken);
    var deleted = redisTokenService.deleteRefreshToken(username, refreshToken);
    if (deleted) {
      var account = (AccountEntity) accountService.loadUserByUsername(username);
      var accessToken = jwtTokenUtils.generateAccessToken(account);
      refreshToken = jwtTokenUtils.generateRefreshToken(account);
      redisTokenService.saveRefreshToken(username, refreshToken);
      return new String[] {accessToken, refreshToken, String.valueOf(account.getId()),
          account.getUsername()};
    }
    throw new InvalidJwtException();
  }
}
