package org.judexmars.imagecrud.dto.image;

import java.io.Serializable;

/**
 * Image DTO for cross-service communication.
 *
 * @param filename name of the image file
 * @param size size of the image in bytes
 * @param link link to the image in storage
 */
public record ImageDto(
    String filename,
    int size,
    String link
) implements Serializable {
}
