package org.judexmars.imagecrud.dto.imagefilters;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/**
 * Response DTO containing id of the request to apply fitlers on image.
 *
 * @param requestId id of the request to apply filters
 */
@Schema(name = "ApplyImageFiltersResponse")
public record ApplyImageFiltersResponse(
    @Schema(description = "ИД запроса в системе", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID requestId
) {
}
