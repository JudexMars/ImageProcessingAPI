package org.judexmars.imageprocessor.dto.imagga.tags

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Tag(
    val tag: TagDetail,
)
