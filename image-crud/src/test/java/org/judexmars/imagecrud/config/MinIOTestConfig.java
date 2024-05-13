package org.judexmars.imagecrud.config;

import java.time.Duration;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@Slf4j
public class MinIOTestConfig {
  private static volatile MinIOContainer minIOContainer = null;

  private static MinIOContainer getMinIOContainer() {
    var instance = minIOContainer;
    if (Objects.isNull(instance)) {
      synchronized (PostgreSQLContainer.class) {
        instance = minIOContainer;
        if (Objects.isNull(instance)) {
          minIOContainer = instance = new MinIOContainer("minio/minio")
              .withUserName("test-user")
              .withPassword("test-pass")
              .withStartupTimeout(Duration.ofSeconds(60))
              .withReuse(true);
          minIOContainer.start();
        }
      }
    }
    return instance;
  }

  @Component("MinioInitializer")
  public static class Initializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
      var minIOContainer = getMinIOContainer();

      var s3url = minIOContainer.getS3URL();
      var username = minIOContainer.getUserName();
      var password = minIOContainer.getPassword();
      log.info("EXPOSED PORTS FOR MINIO: " + minIOContainer.getExposedPorts());
      log.info("HOST FOR MINIO: " + minIOContainer.getHost());
      log.info("ENDPOINT: " + minIOContainer.getS3URL());

      TestPropertyValues.of(
          "minio.bucket=test",
          "minio.url=" + s3url,
          "minio.access-key=" + username,
          "minio.secret-key=" + password,
          "minio.image-size=" + 10485760
      ).applyTo(applicationContext.getEnvironment());

    }
  }
}