package org.judexmars.imagecrud.config;

import java.time.Duration;
import java.util.Objects;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgreTestConfig {
  private static volatile PostgreSQLContainer<?> postgreSQLContainer = null;

  @SuppressWarnings("resource")
  private static PostgreSQLContainer<?> getPostgreSQLContainer() {
    var instance = postgreSQLContainer;
    if (Objects.isNull(instance)) {
      synchronized (PostgreSQLContainer.class) {
        instance = postgreSQLContainer;
        if (Objects.isNull(instance)) {
          postgreSQLContainer = instance = new PostgreSQLContainer<>("postgres:15")
              .withDatabaseName("test-database")
              .withUsername("test-user")
              .withPassword("test-pass")
              .withStartupTimeout(Duration.ofSeconds(60))
              .withReuse(true);
          postgreSQLContainer.start();
        }
      }
    }
    return instance;
  }

  @Component("PostgresInitializer")
  public static class Initializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
      var postgreSQLContainer = getPostgreSQLContainer();

      var jdbcUrl = postgreSQLContainer.getJdbcUrl();
      var username = postgreSQLContainer.getUsername();
      var password = postgreSQLContainer.getPassword();

      TestPropertyValues.of(
          "spring.datasource.url=" + jdbcUrl,
          "spring.datasource.username=" + username,
          "spring.datasource.password=" + password
      ).applyTo(applicationContext.getEnvironment());
    }
  }
}
