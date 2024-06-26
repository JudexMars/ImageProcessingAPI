package org.judexmars.imagecrud.dto.image;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Get images of current user DTO.
 *
 * @param images List of images
 */
@Schema(name = "GetImagesResponse")
public record GetImagesResponseDto(
    @ArraySchema(arraySchema = @Schema(description = "Список изображений",
        requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    List<ImageResponseDto> images
) {
}
