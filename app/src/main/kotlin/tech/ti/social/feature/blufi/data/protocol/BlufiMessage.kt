package tech.ti.social.feature.blufi.data.protocol

/**
 * Represents a decoded BluFi message frame.
 */
data class BlufiMessage(
    val type: Int,
    val data: ByteArray,
    val sequence: Int,
    val encrypted: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BlufiMessage
        return type == other.type &&
            sequence == other.sequence &&
            encrypted == other.encrypted &&
            data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = type
        result = 31 * result + data.contentHashCode()
        result = 31 * result + sequence
        result = 31 * result + (if (encrypted) 1 else 0)
        return result
    }
}
