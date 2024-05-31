package org.judexmars.imageprocessor.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class ClientConfig {
    @Bean
    fun restClient() = RestClient.create()
}
