import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../core/errors/errors.dart';

/// Base hardware manager interface
/// All hardware modules should implement this interface
abstract class HardwareManager {
  /// Hardware type identifier
  String get hardwareType;

  /// Current state of the hardware
  Stream<HardwareState> get state;

  /// Initialize hardware
  Future<Result<void>> initialize();

  /// Release hardware resources
  Future<Result<void>> release();

  /// Check if hardware is available
  Future<bool> isAvailable();

  /// Check if hardware is ready
  bool isReady();
}

/// Hardware state enum
enum HardwareState {
  unavailable,
  initializing,
  ready,
  active,
  error,
  releasing,
}

/// Hardware manager state notifier
class HardwareStateNotifier extends StateNotifier<HardwareState> {
  final HardwareManager _manager;

  HardwareStateNotifier(this._manager) : super(HardwareState.unavailable);

  Future<void> initialize() async {
    state = HardwareState.initializing;
    final result = await _manager.initialize();
    result.when(
      success: () => state = HardwareState.ready,
      failure: (e) => state = HardwareState.error,
    );
  }

  Future<void> release() async {
    state = HardwareState.releasing;
    await _manager.release();
    state = HardwareState.unavailable;
  }

  void setActive() => state = HardwareState.active;
  void setReady() => state = HardwareState.ready;
  void setError() => state = HardwareState.error;
}