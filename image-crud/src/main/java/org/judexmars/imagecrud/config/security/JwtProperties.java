package org.judexmars.imagecrud.config.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String accessSecret;
    private Duration accessLifetime;
    private String refreshSecret;
    private Duration refreshLifetime;
}
