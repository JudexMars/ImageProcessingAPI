package org.judexmars.imagecrud.intergration

import org.judexmars.imagecrud.config.AbstractBaseTest
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.web.WebAppConfiguration


@SpringBootTest
@ExtendWith(SpringExtension::class)
@WebAppConfiguration
class ImageCrudIntegrationTests : AbstractBaseTest() {

    @Test
    fun contextLoad() {

    }
}