package org.judexmars.imagecrud.dto.image;

import java.io.Serializable;

public record ImageDto(
        String filename,
        int size,
        String link
) implements Serializable {
}
