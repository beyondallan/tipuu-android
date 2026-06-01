import 'app_exception.dart';
import 'app_failure.dart';

/// Extension to convert AppException to Failure
extension ExceptionToFailure on AppException {
  Failure toFailure() {
    return when(
      network: (message, code) => Failure.network(message: message),
      server: (message, code, data) => Failure.server(message: message, code: code),
      storage: (message, error) => Failure.storage(message: message),
      hardware: (message, type, error) => Failure.hardware(
        message: message,
        hardwareType: type.name,
      ),
      permission: (message, type) => Failure.permission(
        message: message,
        permissionType: type.name,
      ),
      unknown: (message, error, stackTrace) => Failure.unknown(message: message),
    );
  }
}