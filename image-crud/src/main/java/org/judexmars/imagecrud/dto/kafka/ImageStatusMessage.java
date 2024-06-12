package org.judexmars.imagecrud.dto.kafka;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import org.judexmars.imagecrud.dto.imagefilters.FilterType;

/**
 * Message that is sent to kafka after user requests applying filters.
 *
 * @param imageId   id of the image
 * @param requestId id of the user's request
 * @param filters   list of filter types
 */
public record ImageStatusMessage(
    @Schema(format = "uuid")
    String imageId,
    @Schema(format = "uuid")
    String requestId,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<FilterType> filters,
    Map<String, Object> props
) {
}
