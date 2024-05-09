package org.judexmars.imageprocessor.dto

import java.util.UUID

/**
 * Message that is sent to kafka after user requests applying filters.
 *
 * @param imageId   id of the image
 * @param requestId id of the user's request
 * @param filters   list of filter types
 */
data class ImageStatusMessage(
    val imageId: UUID,
    val requestId: UUID,
    val filters: List<String>,
    val props: Map<String, String>,
)
