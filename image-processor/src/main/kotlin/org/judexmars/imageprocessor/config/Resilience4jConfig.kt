package org.judexmars.imageprocessor.config

import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import java.time.Duration

@Configuration
class Resilience4jConfig {
    @Bean
    fun customRetryConfig(): RetryConfig {
        return RetryConfig.custom<RetryConfig>()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(1000))
            .retryOnException { exception ->
                when (exception) {
                    is HttpClientErrorException -> exception.statusCode == HttpStatus.TOO_MANY_REQUESTS
                    is HttpServerErrorException -> exception.statusCode.is5xxServerError
                    else -> false
                }
            }
            .build()
    }

    @Bean
    fun retryRegistry(config: RetryConfig): RetryRegistry {
        return RetryRegistry.of(config)
    }
}
