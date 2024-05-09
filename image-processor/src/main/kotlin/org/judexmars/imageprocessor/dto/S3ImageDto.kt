package org.judexmars.imageprocessor.dto

data class S3ImageDto(
    val link: String,
    val bytes: ByteArray,
    val contentType: String,
    val size: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as S3ImageDto

        if (link != other.link) return false
        if (!bytes.contentEquals(other.bytes)) return false
        if (contentType != other.contentType) return false
        if (size != other.size) return false

        return true
    }

    override fun hashCode(): Int {
        var result = size.hashCode()
        result = 31 * result + bytes.contentHashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + link.hashCode()
        return result
    }
}
