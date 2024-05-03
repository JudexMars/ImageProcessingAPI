package org.judexmars.imageprocessor.config.minio

import io.minio.MinioClient
import lombok.extern.slf4j.Slf4j
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Minio configuration.
 */
@Configuration
@Slf4j
@EnableConfigurationProperties(
    MinioProperties::class
)
class MinioConfig {
    /**
     * Minio client.
     *
     * @param minioProperties minio properties
     * @return minio client
     */
    @Bean
    fun minioClient(minioProperties: MinioProperties): MinioClient {
        return MinioClient.builder()
            .credentials(minioProperties.accessKey, minioProperties.secretKey)
            .endpoint(minioProperties.url)
            .build()
    }
}
