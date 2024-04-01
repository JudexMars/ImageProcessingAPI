package org.judexmars.imagecrud.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Objects;

@Slf4j
public class RedisTestConfig {

    private static volatile GenericContainer<?> redisContainer = null;

    private static GenericContainer<?> getRedisContainer() {
        var instance = redisContainer;
        if (Objects.isNull(instance)) {
            synchronized (PostgreSQLContainer.class) {
                instance = redisContainer;
                if (Objects.isNull(instance)) {
                    redisContainer = instance = new GenericContainer<>(DockerImageName.parse("redis:7.2.4"))
                            .withExposedPorts(6379)
                            .withCommand("redis-server --save 20 1 --logLevel warning --requirePass 6k_j76,dDUl_")
                            .withStartupTimeout(Duration.ofSeconds(60))
                            .withReuse(true);
                    redisContainer.start();
                }
            }
        }
        return instance;
    }

    @Component("RedisInitializer")
    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            var redisContainer = getRedisContainer();
            log.info("REDIS EXPOSED PORTS: " + redisContainer.getExposedPorts());
            TestPropertyValues.of(
                    "spring.dat.redis.host=" + redisContainer.getHost(),
                    "spring.data.redis.port=" + redisContainer.getFirstMappedPort(),
                    "spring.data.redis.password=6k_j76,dDUl_"
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}
