package org.judexmars.imagecrud.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Upload image response DTO.
 *
 * @param imageId id of the image
 */
@Schema(name = "UploadImageResponse")
public record UploadImageResponseDto(
    @Schema(description = "ИД файла", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    String imageId
) {
}
