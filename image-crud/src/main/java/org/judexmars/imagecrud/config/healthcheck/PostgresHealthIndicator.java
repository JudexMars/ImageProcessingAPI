package org.judexmars.imagecrud.config.healthcheck;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Health indicator for main db.
 */
@Component
@RequiredArgsConstructor
public class PostgresHealthIndicator implements HealthIndicator {

  private final JdbcTemplate jdbcTemplate;

  @Override
  public Health health() {
    try {
      // dumb check that it even responds
      jdbcTemplate.execute("SELECT 1");
      return Health.up().build();
    } catch (Exception e) {
      return Health.down(e).build();
    }
  }
}
