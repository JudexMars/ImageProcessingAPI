package org.judexmars.imagecrud.service;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketLifecycleArgs;
import io.minio.StatObjectArgs;
import io.minio.messages.Expiration;
import io.minio.messages.LifecycleConfiguration;
import io.minio.messages.LifecycleRule;
import io.minio.messages.RuleFilter;
import io.minio.messages.Status;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.judexmars.imagecrud.config.minio.MinioProperties;
import org.judexmars.imagecrud.dto.image.ImageDto;
import org.judexmars.imagecrud.exception.UploadFailedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for working with S3 storage.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {

  private final MinioClient client;

  private final MinioProperties properties;

  /**
   * Initialize S3 storage.
   */
  @PostConstruct
  @SneakyThrows
  public void init() {
    log.info(properties.toString());
    if (!client.bucketExists(BucketExistsArgs.builder()
        .bucket(properties.getMainBucket()).build())) {
      client.makeBucket(MakeBucketArgs.builder().bucket(properties.getMainBucket()).build());
    }
    if (!client.bucketExists(BucketExistsArgs.builder()
        .bucket(properties.getMinorBucket()).build())) {
      client.makeBucket(MakeBucketArgs.builder().bucket(properties.getMinorBucket()).build());

      var rules = new LinkedList<LifecycleRule>();
      rules.add(
          new LifecycleRule(
              Status.ENABLED,
              null,
              new Expiration((ZonedDateTime) null, properties.getTtl(), null),
              new RuleFilter("logs/"),
              "expire-bucket",
              null,
              null,
              null));
      var config = new LifecycleConfiguration(rules);

      client.setBucketLifecycle(SetBucketLifecycleArgs.builder()
          .bucket(properties.getMinorBucket())
          .config(config).build());
    }
  }

  /**
   * Upload new image.
   *
   * @param file binary file
   * @return Image's meta information as {@link ImageDto}
   * @throws Exception if the file cannot be uploaded
   */
  public ImageDto uploadImage(MultipartFile file) throws Exception {
    String fileId = UUID.randomUUID().toString();

    if (!validateImage(file)) {
      throw new UploadFailedException();
    }

    InputStream inputStream = new ByteArrayInputStream(file.getBytes());
    client.putObject(
        PutObjectArgs.builder()
            .bucket(properties.getMainBucket())
            .object(fileId)
            .stream(inputStream, file.getSize(), properties.getImageSize())
            .contentType(file.getContentType())
            .build()
    );

    return new ImageDto(file.getOriginalFilename(), (int) file.getSize(), fileId);
  }

  /**
   * Download image.
   *
   * @param link link to the image in the storage
   * @return binary representation of the image
   * @throws Exception if the image can't be found or downloaded
   */
  public byte[] downloadImage(String link) throws Exception {
    return IOUtils.toByteArray(client.getObject(
        GetObjectArgs.builder()
            .bucket(properties.getMainBucket())
            .object(link)
            .build()
    ));
  }

  private boolean validateImage(MultipartFile file) {
    return validateImageFileSize(file) && validateImageFileType(file);
  }

  private boolean validateImageFileType(MultipartFile file) {
    var type = Objects.requireNonNull(file.getContentType());
    log.info("FILE TYPE: " + type);
    return type.equals("image/png")
        || type.equals("image/jpeg");
  }

  private boolean validateImageFileSize(MultipartFile file) {
    log.info("FILE SIZE: " + file.getSize());
    log.info("MAXIMUM SIZE: " + properties.getImageSize());
    return file.getSize() <= properties.getImageSize();
  }

  /**
   * Delete image.
   *
   * @param link image link
   * @throws Exception if the file cannot be deleted
   */
  public void deleteImage(String link) throws Exception {
    client.removeObject(
        RemoveObjectArgs.builder()
            .bucket(properties.getMainBucket())
            .object(link)
            .build()
    );
  }

  /**
   * Get size of image file.
   *
   * @param link link to the file
   * @return its size in bytes
   * @throws Exception if the file cannot be found
   */
  public long getImageSize(String link) throws Exception {
    var meta = client.statObject(
        StatObjectArgs.builder()
            .bucket(properties.getMainBucket())
            .object(link)
            .build()
    );
    return meta.size();
  }
}
