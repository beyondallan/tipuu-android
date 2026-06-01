import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../domain/entities/ble_device.dart';
import '../domain/services/ble_service.dart';
import '../domain/usecases/ble_usecases.dart';

/// Bluetooth state notifier
class BluetoothNotifier extends StateNotifier<BluetoothState> {
  final BleService _service;

  BluetoothNotifier(this._service) : super(BluetoothState.unavailable) {
    _init();
  }

  void _init() {
    _service.state.listen((state) {
      this.state = state;
    });
  }

  Future<void> scan() async {
    state = BluetoothState.scanning;
  }

  Future<void> stopScan() async {
    await _service.stopScan();
  }
}

/// Bluetooth notifier provider
final bluetoothNotifierProvider =
    StateNotifierProvider<BluetoothNotifier, BluetoothState>((ref) {
  return BluetoothNotifier(ref.watch(bleServiceProvider));
});

/// Scanned devices provider
final scannedDevicesProvider = StateNotifierProvider<ScannedDevicesNotifier, List<BleDevice>>((ref) {
  return ScannedDevicesNotifier(ref.watch(scanDevicesUseCaseProvider));
});

/// Scanned devices notifier
class ScannedDevicesNotifier extends StateNotifier<List<BleDevice>> {
  final ScanDevicesUseCase _scanUseCase;

  ScannedDevicesNotifier(this._scanUseCase) : super([]);

  void startScan({Duration? timeout}) {
    state = [];
    _scanUseCase(timeout: timeout).listen((device) {
      final existing = state.indexWhere((d) => d.id == device.id);
      if (existing >= 0) {
        state = [
          ...state.sublist(0, existing),
          device,
          ...state.sublist(existing + 1),
        ];
      } else {
        state = [...state, device];
      }
    });
  }
}

/// Selected device provider
final selectedDeviceProvider = StateProvider<BleDevice?>((ref) => null);