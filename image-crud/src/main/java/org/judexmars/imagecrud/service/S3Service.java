package org.judexmars.imagecrud.service;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.judexmars.imagecrud.config.minio.MinioProperties;
import org.judexmars.imagecrud.dto.image.ImageDto;
import org.judexmars.imagecrud.exception.UploadFailedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {

    private final MinioClient client;

    private final MinioProperties properties;

    @PostConstruct
    @SneakyThrows
    public void init() throws Exception {
        log.info(properties.toString());
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(properties.getBucket()).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(properties.getBucket()).build());
        }
    }

    /**
     * Upload new image
     *
     * @param file binary file
     * @return Image's meta information as {@link ImageDto}
     * @throws Exception if the file cannot be uploaded
     */
    public ImageDto uploadImage(MultipartFile file) throws Exception {
        String fileId = UUID.randomUUID().toString();

        if (!validateImage(file)) throw new UploadFailedException();

        InputStream inputStream = new ByteArrayInputStream(file.getBytes());
        client.putObject(
                PutObjectArgs.builder()
                        .bucket(properties.getBucket())
                        .object(fileId)
                        .stream(inputStream, file.getSize(), properties.getImageSize())
                        .contentType(file.getContentType())
                        .build()
        );

        return new ImageDto(file.getOriginalFilename(), (int) file.getSize(), fileId);
    }

    /**
     * Download image
     *
     * @param link link to the image in the storage
     * @return binary representation of the image
     * @throws Exception if the image can't be found or downloaded
     */
    public byte[] downloadImage(String link) throws Exception {
        return IOUtils.toByteArray(client.getObject(
                GetObjectArgs.builder()
                        .bucket(properties.getBucket())
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
     * Delete image
     *
     * @param link image link
     * @throws Exception if the file cannot be deleted
     */
    public void deleteImage(String link) throws Exception {
        client.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(properties.getBucket())
                        .object(link)
                        .build()
        );
    }
}
