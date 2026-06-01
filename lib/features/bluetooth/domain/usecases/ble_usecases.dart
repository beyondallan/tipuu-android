import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/core.dart';
import '../entities/ble_device.dart';
import '../services/ble_service.dart';

/// Scan for BLE devices use case
class ScanDevicesUseCase {
  final BleService _service;

  ScanDevicesUseCase(this._service);

  Stream<BleDevice> call({Duration? timeout}) {
    return _service.scan(timeout: timeout);
  }
}

/// Connect to device use case
class ConnectDeviceUseCase {
  final BleService _service;

  ConnectDeviceUseCase(this._service);

  Future<Result<void>> call(String deviceId) {
    return _service.connect(deviceId);
  }
}

/// Disconnect from device use case
class DisconnectDeviceUseCase {
  final BleService _service;

  DisconnectDeviceUseCase(this._service);

  Future<Result<void>> call(String deviceId) {
    return _service.disconnect(deviceId);
  }
}

/// Send data use case
class SendDataUseCase {
  final BleService _service;

  SendDataUseCase(this._service);

  Future<Result<void>> call(
    String deviceId,
    String serviceUuid,
    String characteristicUuid,
    List<int> data,
  ) {
    return _service.sendData(
      deviceId,
      serviceUuid,
      characteristicUuid,
      data,
    );
  }
}

/// Use case providers
final scanDevicesUseCaseProvider = Provider<ScanDevicesUseCase>((ref) {
  return ScanDevicesUseCase(ref.watch(bleServiceProvider));
});

final connectDeviceUseCaseProvider = Provider<ConnectDeviceUseCase>((ref) {
  return ConnectDeviceUseCase(ref.watch(bleServiceProvider));
});

final disconnectDeviceUseCaseProvider = Provider<DisconnectDeviceUseCase>((ref) {
  return DisconnectDeviceUseCase(ref.watch(bleServiceProvider));
});

final sendDataUseCaseProvider = Provider<SendDataUseCase>((ref) {
  return SendDataUseCase(ref.watch(bleServiceProvider));
});