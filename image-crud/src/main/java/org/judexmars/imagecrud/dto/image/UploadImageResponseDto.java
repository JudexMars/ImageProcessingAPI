package org.judexmars.imagecrud.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UploadImageResponse")
public record UploadImageResponseDto(
        @Schema(description = "ИД файла", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
        String imageId
) {
}
