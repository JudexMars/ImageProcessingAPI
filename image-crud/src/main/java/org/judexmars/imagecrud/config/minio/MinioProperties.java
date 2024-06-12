package org.judexmars.imagecrud.config.minio;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Minio.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "minio")
@ToString
public class MinioProperties {

  private String url;

  private int port;

  private String accessKey;

  private String secretKey;

  private boolean secure;

  private String mainBucket;

  private String minorBucket;

  private long imageSize;

  private int ttl;
}
