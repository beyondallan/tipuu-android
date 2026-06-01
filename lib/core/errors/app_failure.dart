import 'package:freezed_annotation/freezed_annotation.dart';

part 'app_failure.freezed.dart';

/// Failure class for domain layer
/// Used to represent errors in a clean way without exposing exceptions
@freezed
class Failure with _$Failure {
  const factory Failure.network({
    required String message,
  }) = NetworkFailure;

  const factory Failure.server({
    required String message,
    int? code,
  }) = ServerFailure;

  const factory Failure.storage({
    required String message,
  }) = StorageFailure;

  const factory Failure.hardware({
    required String message,
    required String hardwareType,
  }) = HardwareFailure;

  const factory Failure.permission({
    required String message,
    required String permissionType,
  }) = PermissionFailure;

  const factory Failure.validation({
    required String message,
    Map<String, String>? errors,
  }) = ValidationFailure;

  const factory Failure.unknown({
    required String message,
  }) = UnknownFailure;
}