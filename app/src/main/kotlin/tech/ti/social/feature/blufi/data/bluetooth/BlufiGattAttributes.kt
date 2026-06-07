package tech.ti.social.feature.blufi.data.bluetooth

import java.util.UUID

object BlufiGattAttributes {

    // GATT UUIDs
    val SERVICE_UUID: UUID = UUID.fromString("0000FFFF-0000-1000-8000-00805F9B34FB")
    val CONTROL_CHAR_UUID: UUID = UUID.fromString("0000FF01-0000-1000-8000-00805F9B34FB")
    val DATA_CHAR_UUID: UUID = UUID.fromString("0000FF02-0000-1000-8000-00805F9B34FB")

    // Frame headers
    const val HEADER_STANDARD = 0x00
    const val HEADER_ENCRYPTED = 0x01

    // Message types
    // Control frames (0x00-0x0F)
    const val TYPE_ACK = 0x00
    const val TYPE_HELLO = 0x01
    const val TYPE_BYE = 0x02

    // Security frames (0x10-0x1F)
    const val TYPE_NEG_SEC_START = 0x10
    const val TYPE_NEG_SEC_STOP = 0x11
    const val TYPE_SET_SEC_CONF = 0x12
    const val TYPE_SET_PSK = 0x13
    const val TYPE_NEG_SEC_RSP = 0x14

    // Data frames (0x20-0x2F)
    const val TYPE_RECV_STA_BSSID = 0x20
    const val TYPE_RECV_STA_SSID = 0x21
    const val TYPE_RECV_STA_PASSWD = 0x22
    const val TYPE_RECV_WIFI_LIST = 0x23
    const val TYPE_REPORT_WIFI_CONNECTION = 0x24
    const val TYPE_GET_WIFI_LIST = 0x25
    const val TYPE_REQ_CONNECT_TO_AP = 0x26
    const val TYPE_REQ_DISCONNECT_FROM_AP = 0x27
    const val TYPE_GET_WIFI_STATUS = 0x28
    const val TYPE_DEINIT = 0x29
    const val TYPE_SET_WIFI_MODE = 0x2A

    // Custom data
    const val TYPE_SEND_CUSTOM_DATA = 0x30

    // Security sub-types (inside NEG_SEC_RSP data)
    const val SEC_SUBTYPE_DH_PARAM = 0x00
    const val SEC_SUBTYPE_DH_PUB_KEY = 0x01

    // Connection status values
    const val STATUS_SUCCESS = 0x00
    const val STATUS_FAIL = 0x01
    const val STATUS_CONNECTING = 0x02

    // Device name prefix to scan for
    const val DEVICE_NAME_PREFIX = "Xiaozhi"

    // Timeouts (ms)
    const val TIMEOUT_HELLO = 5000L
    const val TIMEOUT_SECURITY = 10000L
    const val TIMEOUT_WIFI_LIST = 15000L
    const val TIMEOUT_CONNECTION_REPORT = 30000L

    // Max MTU for BLE
    const val MAX_MTU = 512
}
