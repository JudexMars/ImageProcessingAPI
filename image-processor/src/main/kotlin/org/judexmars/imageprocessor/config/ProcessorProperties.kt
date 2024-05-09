package org.judexmars.imageprocessor.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "props")
data class ProcessorProperties(
    val type: String,
    val group: String,
    val concurrency: Int,
    val wipTopic: String,
    val doneTopic: String,
)
