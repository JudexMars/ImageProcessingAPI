package org.judexmars.imagecrud.service;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for managing tokens in Redis.
 */
@Service
@RequiredArgsConstructor
public class RedisTokenService {

  private final RedisTemplate<String, String> redisTemplate;

  private final JwtTokenService jwtTokenService;

  /**
   * Deletes refresh token from db.
   *
   * @param username     username of the user who holds this token
   * @param refreshToken provided refresh token
   * @return {@code true} if deleted successfully
   */
  public boolean deleteRefreshToken(String username, String refreshToken) {
    String key = "user:" + username + ":refresh_tokens";
    var count = Objects.requireNonNull(redisTemplate.opsForList().range(key, 0, -1)).stream()
        .filter(token -> token.equals(refreshToken))
        .count();
    if (count > 0) {
      redisTemplate.opsForList().remove(key, count, refreshToken);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Save user's refresh token to db.
   *
   * @param username     provided username
   * @param refreshToken token to be saved
   */
  public void saveRefreshToken(String username, String refreshToken) {
    var expirationInSeconds =
        jwtTokenService.getExpirationDateFromRefreshToken(refreshToken).getTime()
            - new Date().getTime() / 1000;
    String key = "user:" + username + ":refresh_tokens";
    redisTemplate.opsForList().rightPush(key, refreshToken);
    redisTemplate.expire(key, expirationInSeconds, TimeUnit.SECONDS);
  }
}
