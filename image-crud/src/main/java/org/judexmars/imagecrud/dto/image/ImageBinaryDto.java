package org.judexmars.imagecrud.dto.image;

public record ImageBinaryDto(
    String filename,
    int size,
    byte[] data
) {
}
