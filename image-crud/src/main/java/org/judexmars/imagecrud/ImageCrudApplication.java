package org.judexmars.imagecrud;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.judexmars.imagecrud.config.minio.MinioProperties;
import org.judexmars.imagecrud.config.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main application class.
 */
@SpringBootApplication
@SecurityScheme(name = "Auth",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    in = SecuritySchemeIn.HEADER,
    bearerFormat = "JWT")
@EnableConfigurationProperties({JwtProperties.class, MinioProperties.class})
public class ImageCrudApplication {

  public static void main(String[] args) {
    SpringApplication.run(ImageCrudApplication.class, args);
  }

}
