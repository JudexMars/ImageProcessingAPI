package org.judexmars.imageprocessor.config.healthcheck

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

/**
 * Health indicator for secondary db.
 */
@Component
class RedisHealthIndicator(
    private val redisTemplate: StringRedisTemplate,
) : HealthIndicator {
    override fun health(): Health {
        try {
            val result = redisTemplate.connectionFactory?.connection?.ping()
            return if ("PONG" == result) {
                Health.up().build()
            } else {
                Health.down().withDetail("ping", result).build()
            }
        } catch (e: Exception) {
            return Health.down(e).build()
        }
    }
}
