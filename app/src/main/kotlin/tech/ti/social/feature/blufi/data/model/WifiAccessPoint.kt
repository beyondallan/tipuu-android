package tech.ti.social.feature.blufi.data.model

data class WifiAccessPoint(
    val ssid: String,
    val rssi: Int,
    val authMode: AuthMode,
    val channel: Int = 0
) {
    val displayName: String
        get() = if (ssid.isBlank()) "<Unknown>" else ssid

    enum class AuthMode(val label: String) {
        OPEN("Open"),
        WEP("WEP"),
        WPA_PSK("WPA-PSK"),
        WPA2_PSK("WPA2-PSK"),
        WPA_WPA2_PSK("WPA/WPA2-PSK"),
        WPA3_PSK("WPA3-PSK"),
        UNKNOWN("Unknown");

        companion object {
            fun fromValue(value: Int): AuthMode = when (value) {
                0 -> OPEN
                1 -> WEP
                3 -> WPA_PSK
                4 -> WPA2_PSK
                5 -> WPA_WPA2_PSK
                6 -> WPA3_PSK
                else -> UNKNOWN
            }
        }
    }
}
