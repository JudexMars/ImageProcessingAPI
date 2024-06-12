package org.judexmars.imageprocessor.config.minio

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "minio")
data class MinioProperties(
    val url: String,
    val accessKey: String,
    val secretKey: String,
    val mainBucket: String,
    val minorBucket: String,
    val imageSize: Long,
)
