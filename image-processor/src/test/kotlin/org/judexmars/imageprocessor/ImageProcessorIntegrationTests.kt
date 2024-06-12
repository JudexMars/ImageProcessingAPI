package org.judexmars.imageprocessor

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(classes = [ImageProcessorApplication::class])
@ExtendWith(SpringExtension::class)
class ImageProcessorIntegrationTests {
    @Test
    fun contextLoads() {
    }
}
