package org.judexmars.imagecrud.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Properties for JWT token generation and validation.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String accessSecret;
    private Duration accessLifetime;
    private String refreshSecret;
    private Duration refreshLifetime;
}
