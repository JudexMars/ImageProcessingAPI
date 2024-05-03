package org.judexmars.imageprocessor.service

import org.judexmars.imageprocessor.dto.ImageStatusMessage

interface Processor {
    fun process(message: ImageStatusMessage)
}