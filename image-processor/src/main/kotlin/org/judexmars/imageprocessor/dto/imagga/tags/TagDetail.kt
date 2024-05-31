package org.judexmars.imageprocessor.dto.imagga.tags

import com.fasterxml.jackson.annotation.JsonProperty

data class TagDetail(
    @JsonProperty("en")
    val name: String,
)
