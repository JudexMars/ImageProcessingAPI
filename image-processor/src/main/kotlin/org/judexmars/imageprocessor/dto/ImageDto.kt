package org.judexmars.imageprocessor.dto

/**
 * Image DTO for cross-service communication.
 *
 * @param size     size of the image in bytes
 * @param link     link to the image in storage
 * @param bytes byte representation of image
 */
@JvmRecord
data class ImageDto(
    val size: Long,
    val link: String,
    val bytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageDto

        if (size != other.size) return false
        if (link != other.link) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = size.hashCode()
        result = 31 * result + link.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}
