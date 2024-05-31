package org.judexmars.imageprocessor.dto.imagga.usage

import org.judexmars.imageprocessor.dto.imagga.Status

data class UsageResponse(
    val result: UsageResult?,
    val status: Status,
)
