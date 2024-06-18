package org.judexmars.imageprocessor.dto.imagga.upload

import com.fasterxml.jackson.annotation.JsonProperty

data class UploadResult(
    @JsonProperty("upload_id")
    val uploadId: String,
)
