import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:permission_handler/permission_handler.dart';
import '../core/errors/errors.dart';

/// Permission service provider
final permissionServiceProvider = Provider<PermissionService>((ref) {
  return PermissionService();
});

/// Permission service for handling hardware permissions
class PermissionService {
  /// Check if permission is granted
  Future<bool> isGranted(Permission permission) async {
    final status = await permission.status;
    return status == PermissionStatus.granted;
  }

  /// Request permission
  Future<PermissionResult> request(Permission permission) async {
    final status = await permission.request();
    return _mapStatusToResult(status, permission);
  }

  /// Request multiple permissions
  Future<Map<Permission, PermissionResult>> requestMultiple(
    List<Permission> permissions,
  ) async {
    final results = await permissions.request();
    return results.map((permission, status) =>
        MapEntry(permission, _mapStatusToResult(status, permission)));
  }

  /// Check and request permission if not granted
  Future<PermissionResult> checkAndRequest(Permission permission) async {
    if (await isGranted(permission)) {
      return PermissionResult.granted;
    }
    return request(permission);
  }

  /// Open app settings for manual permission grant
  Future<bool> openSettings() async {
    return await openAppSettings();
  }

  PermissionResult _mapStatusToResult(
    PermissionStatus status,
    Permission permission,
  ) {
    switch (status) {
      case PermissionStatus.granted:
        return PermissionResult.granted;
      case PermissionStatus.denied:
        return PermissionResult.denied;
      case PermissionStatus.restricted:
        return PermissionResult.restricted;
      case PermissionStatus.limited:
        return PermissionResult.limited;
      case PermissionStatus.permanentlyDenied:
        return PermissionResult.permanentlyDenied;
      case PermissionStatus.provisional:
        return PermissionResult.provisional;
    }
  }
}

/// Permission result enum
enum PermissionResult {
  granted,
  denied,
  restricted,
  limited,
  permanentlyDenied,
  provisional,
}

/// Extension to convert PermissionResult to Failure
extension PermissionResultExtension on PermissionResult {
  Failure? toFailure(String message) {
    if (this == PermissionResult.granted) return null;
    return Failure.permission(
      message: message,
      permissionType: this.name,
    );
  }
}