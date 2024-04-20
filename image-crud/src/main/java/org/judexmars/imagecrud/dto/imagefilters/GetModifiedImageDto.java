package org.judexmars.imagecrud.dto.imagefilters;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/**
 * Response DTO containing image id and request status.
 */
@Schema(name = "GetModifiedImageByRequestIdResponse")
public record GetModifiedImageDto(
    @Schema(description = """
        ИД модифицированного или оригинального файла\
        в случае отсутствия первого""",
        requiredMode = Schema.RequiredMode.REQUIRED)
    UUID imageId,
    @Schema(description = "Статус обработки файла",
        requiredMode = Schema.RequiredMode.REQUIRED)
    BasicRequestStatus status
) {
}
