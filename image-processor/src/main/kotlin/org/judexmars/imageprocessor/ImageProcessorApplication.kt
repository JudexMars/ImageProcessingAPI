package org.judexmars.imageprocessor

import org.judexmars.imageprocessor.config.ImaggaProperties
import org.judexmars.imageprocessor.config.ProcessorProperties
import org.judexmars.imageprocessor.config.minio.MinioProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(ProcessorProperties::class, MinioProperties::class, ImaggaProperties::class)
class ImageProcessorApplication

fun main(args: Array<String>) {
    runApplication<ImageProcessorApplication>(*args)
}
