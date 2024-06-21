package org.judexmars.imagecrud.config.healthcheck;

import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

/**
 * Health indicator for kafka brokers.
 */
@Component
@RequiredArgsConstructor
public class KafkaHealthIndicator implements HealthIndicator {

  private final KafkaAdmin kafkaAdmin;

  @Override
  public Health health() {
    try (var adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
      var describeClusterResult = adminClient.describeCluster();
      int nodeCount = describeClusterResult.nodes().get().size();
      if (nodeCount > 0) {
        return Health.up().withDetail("nodeCount", nodeCount).build();
      } else {
        return Health.down().withDetail("nodeCount", nodeCount).build();
      }
    } catch (InterruptedException | ExecutionException e) {
      return Health.down(e).build();
    }
  }
}
