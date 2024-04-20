package org.judexmars.imagecrud.config.minio;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minio configuration.
 */
@Configuration
@Slf4j
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

  /**
   * Minio client.
   *
   * @param minioProperties minio properties
   * @return minio client
   */
  @Bean
  public MinioClient minioClient(MinioProperties minioProperties) {
    log.info("MINIO CLIENT: " + minioProperties.toString());
    return MinioClient.builder()
        .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
        .endpoint(minioProperties.getUrl())
        .build();
  }
}
