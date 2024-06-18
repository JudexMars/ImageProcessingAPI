package org.judexmars.imageprocessor.dto.imagga.usage

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class UsageResult(
    @JsonProperty("monthly_limit")
    val monthlyLimit: Int,
    @JsonProperty("monthly_processed")
    val monthlyProcessed: Int,
)
