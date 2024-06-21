package org.judexmars.imageprocessor.config

import io.micrometer.core.aop.CountedAspect
import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy

/**
 * Configuration for Micrometer.
 */
@Configuration
@EnableAspectJAutoProxy
class MicrometerConfig {
    @Bean
    fun countedAspect(registry: MeterRegistry?): CountedAspect {
        return CountedAspect(registry!!)
    }

    @Bean
    fun timedAspect(registry: MeterRegistry?): TimedAspect {
        return TimedAspect(registry!!)
    }
}
