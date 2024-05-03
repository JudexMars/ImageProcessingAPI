package org.judexmars.imageprocessor.service

import io.minio.*
import jakarta.annotation.PostConstruct
import lombok.SneakyThrows
import org.apache.commons.compress.utils.IOUtils
import org.judexmars.imageprocessor.config.minio.*
import org.judexmars.imageprocessor.config.minio.MinioProperties
import org.judexmars.imageprocessor.dto.ImageDto
import org.judexmars.imageprocessor.dto.S3ImageDto
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*

/**
 * Service for working with S3 storage.
 */
@Service
class S3Service(
    val client: MinioClient,
    val properties: MinioProperties
) {


    /**
     * Initialize S3 storage.
     */
    @PostConstruct
    @SneakyThrows
    fun init() {
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(properties.mainBucket).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(properties.mainBucket).build())
        }
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(properties.minorBucket).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(properties.minorBucket).build())
        }
    }

    /**
     * Upload new image.
     *
     * @param bytes binary file
     * @return Image's meta information as [ImageDto]
     * @throws Exception if the file cannot be uploaded
     */
    @Throws(Exception::class)
    fun uploadImage(bytes: ByteArray, contentType: String, size: Long, isIntermediate: Boolean): S3ImageDto {
        val link = UUID.randomUUID().toString()
        val inputStream: InputStream = ByteArrayInputStream(bytes)
        client.putObject(
            PutObjectArgs.builder()
                .bucket(if (isIntermediate) properties.minorBucket else properties.mainBucket)
                .`object`(link)
                .stream(inputStream, size, properties.imageSize)
                .contentType(contentType)
                .build()
        )

        return S3ImageDto(link, bytes, contentType, size)
    }

    /**
     * Download image.
     *
     * @param link link to the image in the storage
     * @return binary representation of the image
     * @throws Exception if the image can't be found or downloaded
     */
    @Throws(Exception::class)
    fun downloadImage(link: String, isIntermediate: Boolean): S3ImageDto {
        val bucket = when (isIntermediate) {
            true -> properties.mainBucket
            false -> properties.minorBucket
        }
        val image = client.getObject(
            GetObjectArgs.builder()
                .bucket(bucket)
                .`object`(link)
                .build()
        )
        val meta = client.statObject(
            StatObjectArgs.builder()
                .bucket(bucket)
                .`object`(link)
                .build()
        )
        return S3ImageDto(link, IOUtils.toByteArray(image), meta.contentType(), meta.size())
    }
}
