package org.judexmars.imagecrud.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UiSuccessContainer")
public record BaseResponseDto(
        @Schema(description = "Признак успеха", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean success,
        @Schema(description = "Сообщение об ошибке")
        String message
) {
}
