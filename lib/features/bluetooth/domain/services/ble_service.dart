import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/core.dart';
import '../entities/ble_device.dart';

/// Bluetooth state enum
enum BluetoothState {
  unavailable,
  off,
  on,
  scanning,
  connected,
  error,
}

/// BLE service interface
abstract class BleService {
  /// Current bluetooth state
  Stream<BluetoothState> get state;

  /// Check if bluetooth is available and on
  Future<bool> isReady();

  /// Turn on bluetooth (if supported)
  Future<Result<void>> turnOn();

  /// Start scanning for devices
  Stream<BleDevice> scan({Duration? timeout});

  /// Stop scanning
  Future<void> stopScan();

  /// Connect to a device
  Future<Result<void>> connect(String deviceId);

  /// Disconnect from a device
  Future<Result<void>> disconnect(String deviceId);

  /// Get connected devices
  Future<List<BleDevice>> getConnectedDevices();

  /// Send data to a device
  Future<Result<void>> sendData(
    String deviceId,
    String serviceUuid,
    String characteristicUuid,
    List<int> data,
  );

  /// Receive data from a device
  Stream<List<int>> receiveData(
    String deviceId,
    String serviceUuid,
    String characteristicUuid,
  );

  /// Discover services of a device
  Future<Result<List<String>>> discoverServices(String deviceId);
}

/// BLE service provider
final bleServiceProvider = Provider<BleService>((ref) {
  throw UnimplementedError('bleServiceProvider must be overridden');
});