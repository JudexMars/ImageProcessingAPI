package org.judexmars.imageprocessor.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "imagga")
data class ImaggaProperties(
    val uri: String = "https://based.website.com",
    val apiKey: String = "apiKey",
    val apiSecret: String = "apiSecret",
)
