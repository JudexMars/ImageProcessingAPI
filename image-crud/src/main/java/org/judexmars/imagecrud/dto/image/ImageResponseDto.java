package org.judexmars.imagecrud.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Image")
public record ImageResponseDto(
        @Schema(description = "ИД файла", format = "uuid")
        String imageId,
        @Schema(description = "Название изображения", requiredMode = Schema.RequiredMode.REQUIRED)
        String filename,
        @Schema(description = "Размер файла в байтах", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer size
) {
}
