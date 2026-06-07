package tech.ti.social.feature.blufi.data.bluetooth

import android.util.Log
import tech.ti.social.feature.blufi.data.bluetooth.BlufiGattAttributes.HEADER_ENCRYPTED
import tech.ti.social.feature.blufi.data.bluetooth.BlufiGattAttributes.HEADER_STANDARD
import tech.ti.social.feature.blufi.data.protocol.BlufiMessage

private const val TAG = "BluFiFrameEncoder"

/**
 * BluFi frame encoder/decoder.
 *
 * Frame format:
 * | Header(1B) | Type(1B) | Length(2B LE) | Seq(1B) | Data(NB) | CRC16(2B BE) |
 *
 * Header: 0x00 = standard, 0x01 = encrypted
 * CRC16-BE: computed from Type byte through end of Data (polynomial 0x1021, init 0x00)
 */
class BlufiFrameEncoder {

    private var sequence: Int = 0

    /**
     * Encode a BluFi frame.
     */
    fun encode(type: Int, data: ByteArray? = null, encrypted: Boolean = false): ByteArray {
        val payload = data ?: ByteArray(0)
        val dataLen = payload.size
        val frame = ByteArray(6 + dataLen + 2) // header + type + length(2) + seq + data + crc(2)

        var pos = 0
        frame[pos++] = (if (encrypted) HEADER_ENCRYPTED else HEADER_STANDARD).toByte()
        frame[pos++] = type.toByte()
        frame[pos++] = (dataLen and 0xFF).toByte()
        frame[pos++] = ((dataLen shr 8) and 0xFF).toByte()
        frame[pos++] = (sequence and 0xFF).toByte()

        if (payload.isNotEmpty()) {
            System.arraycopy(payload, 0, frame, pos, dataLen)
            pos += dataLen
        }

        // CRC16 computed from type byte (index 1) to end of data (index pos-1)
        val crc = crc16(frame, 1, pos)
        frame[pos++] = (crc and 0xFF).toByte()
        frame[pos] = ((crc shr 8) and 0xFF).toByte()

        Log.d(TAG, "encode type=0x${type.toString(16)} seq=$sequence len=$dataLen encrypted=$encrypted")

        val seq = sequence
        sequence++
        return frame
    }

    /**
     * Decode a raw BluFi frame.
     */
    fun decode(raw: ByteArray): BlufiMessage? {
        if (raw.size < 6) {
            Log.w(TAG, "decode: frame too short (${raw.size} bytes)")
            return null
        }

        val header = raw[0].toInt() and 0xFF
        val type = raw[1].toInt() and 0xFF
        val dataLen = (raw[2].toInt() and 0xFF) or ((raw[3].toInt() and 0xFF) shl 8)
        val seq = raw[4].toInt() and 0xFF
        val encrypted = (header and 0x01) != 0

        val expectedLen = 6 + dataLen + 2
        if (raw.size < expectedLen) {
            Log.w(TAG, "decode: frame size ${raw.size} < expected $expectedLen")
            return null
        }

        val dataStart = 5
        val data = ByteArray(dataLen)
        System.arraycopy(raw, dataStart, data, 0, dataLen)

        // Validate CRC
        val crcStart = 1
        val crcEnd = dataStart + dataLen
        val expectedCrc = crc16(raw, crcStart, crcEnd)
        val receivedCrc = (raw[crcEnd].toInt() and 0xFF) or ((raw[crcEnd + 1].toInt() and 0xFF) shl 8)

        if (expectedCrc != receivedCrc) {
            Log.w(TAG, "decode: CRC mismatch expected=0x${expectedCrc.toString(16)} received=0x${receivedCrc.toString(16)}")
            return null
        }

        return BlufiMessage(
            type = type,
            data = data,
            sequence = seq,
            encrypted = encrypted
        )
    }

    /**
     * CRC16-BE (CCITT) with polynomial 0x1021.
     * BluFi uses init=0x00 (not the standard 0xFFFF).
     * Computed over data[offset]..data[offset+length-1].
     */
    fun crc16(data: ByteArray, offset: Int, end: Int): Int {
        var crc = 0
        for (i in offset until end) {
            crc = crc xor ((data[i].toInt() and 0xFF) shl 8)
            repeat(8) {
                crc = if ((crc and 0x8000) != 0) {
                    (crc shl 1) xor 0x1021
                } else {
                    crc shl 1
                }
                crc = crc and 0xFFFF
            }
        }
        return crc
    }

    /**
     * Reset sequence number.
     */
    fun resetSequence() {
        sequence = 0
    }
}
