package org.judexmars.imagecrud.config.healthcheck;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Health indicator for secondary db.
 */
@Component
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {

  private final StringRedisTemplate redisTemplate;

  @Override
  public Health health() {
    try {
      String result = redisTemplate.getConnectionFactory().getConnection().ping();
      if ("PONG".equals(result)) {
        return Health.up().build();
      } else {
        return Health.down().withDetail("ping", result).build();
      }
    } catch (Exception e) {
      return Health.down(e).build();
    }
  }
}
