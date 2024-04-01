package org.judexmars.imagecrud.dto.image;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "GetImagesResponse")
public record GetImagesResponseDto(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @ArraySchema(arraySchema = @Schema(description = "Список изображений", requiredMode = Schema.RequiredMode.REQUIRED))
        List<ImageResponseDto> images
) {
}
