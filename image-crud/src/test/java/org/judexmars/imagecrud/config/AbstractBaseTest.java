package org.judexmars.imagecrud.config;

import org.judexmars.imagecrud.config.kafka.KafkaConfiguration;
import org.judexmars.imagecrud.service.ModifiedImagesKafkaConsumer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@MockBean(classes = {KafkaConfiguration.class,
    KafkaTemplate.class, ModifiedImagesKafkaConsumer.class})
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
