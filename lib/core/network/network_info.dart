import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Network connectivity provider
final connectivityProvider = Provider<Connectivity>((ref) {
  return Connectivity();
});

/// Network status stream provider
final networkStatusProvider = StreamProvider<NetworkStatus>((ref) {
  final connectivity = ref.watch(connectivityProvider);
  return connectivity.onConnectivityChanged.map((result) {
    if (result.contains(ConnectivityResult.mobile) ||
        result.contains(ConnectivityResult.wifi)) {
      return NetworkStatus.connected;
    }
    return NetworkStatus.disconnected;
  });
});

/// Current network status provider
final isConnectedProvider = Provider<bool>((ref) {
  final status = ref.watch(networkStatusProvider);
  return status.when(
    data: (s) => s == NetworkStatus.connected,
    loading: () => false,
    error: (_, __) => false,
  );
});

enum NetworkStatus {
  connected,
  disconnected,
}