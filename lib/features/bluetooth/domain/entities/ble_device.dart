import 'package:freezed_annotation/freezed_annotation.dart';

part 'ble_device.freezed.dart';
part 'ble_device.g.dart';

/// BLE device entity
@freezed
class BleDevice with _$BleDevice {
  const factory BleDevice({
    required String id,
    required String name,
    String? address,
    @JsonKey(name: 'rssi') int? rssi,
    @Default(false) bool isConnected,
    @Default(false) bool isConnecting,
    @JsonKey(name: 'service_uuids') List<String>? serviceUuids,
    @JsonKey(name: 'last_seen') DateTime? lastSeen,
  }) = _BleDevice;

  factory BleDevice.fromJson(Map<String, dynamic> json) => _$BleDeviceFromJson(json);
}