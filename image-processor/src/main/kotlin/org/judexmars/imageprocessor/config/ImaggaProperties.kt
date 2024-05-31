package org.judexmars.imageprocessor.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "imagga")
data class ImaggaProperties(
    val uri: String,
    val apiKey: String,
    val apiSecret: String,
)
