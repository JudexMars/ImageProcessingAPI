package org.judexmars.imageprocessor.dto.imagga.upload

import org.judexmars.imageprocessor.dto.imagga.Status

data class UploadResponse(
    // if the operation is not successful, this property is absent/empty
    val result: UploadResult?,
    val status: Status,
)
