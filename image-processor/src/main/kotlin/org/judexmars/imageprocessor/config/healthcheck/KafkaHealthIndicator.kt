package org.judexmars.imageprocessor.config.healthcheck

import org.apache.kafka.clients.admin.AdminClient
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.stereotype.Component

/**
 * Health indicator for kafka brokers.
 */
@Component
class KafkaHealthIndicator(
    private val kafkaAdmin: KafkaAdmin,
) : HealthIndicator {
    override fun health(): Health {
        try {
            AdminClient.create(kafkaAdmin.configurationProperties).use { adminClient ->
                val describeClusterResult = adminClient.describeCluster()
                val nodeCount = describeClusterResult.nodes().get().size
                return if (nodeCount > 0) {
                    Health.up().withDetail("nodeCount", nodeCount).build()
                } else {
                    Health.down().withDetail("nodeCount", nodeCount).build()
                }
            }
        } catch (e: Exception) {
            return Health.down(e).build()
        }
    }
}
