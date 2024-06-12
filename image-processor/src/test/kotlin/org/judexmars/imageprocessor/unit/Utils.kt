package org.judexmars.imageprocessor.unit

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.color.RGBColor
import com.sksamuel.scrimage.nio.JpegWriter
import com.sksamuel.scrimage.nio.PngWriter
import com.sksamuel.scrimage.pixels.Pixel

fun createTestJpegImage(): ByteArray {
    val image = ImmutableImage.create(200, 200)
    return image.bytes(JpegWriter.CompressionFromMetaData)
}

fun createSimpleColorTestPngImage(): ByteArray {
    val image = ImmutableImage.create(4, 1)
    image.setPixel(Pixel(0, 0, RGBColor(0, 0, 255).toARGBInt()))
    image.setPixel(Pixel(1, 0, RGBColor(0, 255, 0).toARGBInt()))
    image.setPixel(Pixel(2, 0, RGBColor(255, 0, 0).toARGBInt()))
    image.setPixel(Pixel(3, 0, RGBColor(255, 255, 255).toARGBInt()))
    return image.bytes(PngWriter.NoCompression)
}
