import 'package:freezed_annotation/freezed_annotation.dart';

part 'app_exception.freezed.dart';

/// Base exception class for the application
@freezed
class AppException with Exception implements Exception {
  const factory AppException.network({
    required String message,
    int? code,
  }) = NetworkException;

  const factory AppException.server({
    required String message,
    required int code,
    dynamic data,
  }) = ServerException;

  const factory AppException.storage({
    required String message,
    dynamic error,
  }) = StorageException;

  const factory AppException.hardware({
    required String message,
    required HardwareType type,
    dynamic error,
  }) = HardwareException;

  const factory AppException.permission({
    required String message,
    required PermissionType type,
  }) = PermissionException;

  const factory AppException.unknown({
    required String message,
    dynamic error,
    StackTrace? stackTrace,
  }) = UnknownException;
}

enum HardwareType {
  bluetooth,
  audio,
  wifi,
  nfc,
}

enum PermissionType {
  microphone,
  bluetooth,
  location,
  storage,
  camera,
}