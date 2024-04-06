package org.judexmars.imagecrud.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@DirtiesContext
@ContextConfiguration(initializers = {PostgreTestConfig.Initializer.class,
    MinIOTestConfig.Initializer.class, RedisTestConfig.Initializer.class})
@Testcontainers
public abstract class AbstractBaseTest {
  @Container
  public GenericContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15");

  @Container
  public GenericContainer<?> minioContainer = new MinIOContainer("minio/minio");
}
