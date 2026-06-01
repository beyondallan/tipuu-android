import 'package:dio/dio.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

import '../errors/app_exception.dart';

part 'api_result.freezed.dart';

/// Result wrapper for API calls
@freezed
class ApiResult<T> with _$ApiResult<T> {
  const factory ApiResult.success(T data) = Success<T>;
  const factory ApiResult.failure(AppException exception) = Failure<T>;
}

/// Extension to handle API results
extension ApiResultExtension<T> on ApiResult<T> {
  R when<R>({
    required R Function(T data) success,
    required R Function(AppException exception) failure,
  }) {
    return map(
      success: (s) => success(s.data),
      failure: (f) => failure(f.exception),
    );
  }

  T? get dataOrNull => when(
    success: (data) => data,
    failure: (_) => null,
  );

  AppException? get exceptionOrNull => when(
    success: (_) => null,
    failure: (exception) => exception,
  );

  bool get isSuccess => when(
    success: (_) => true,
    failure: (_) => false,
  );
}

/// Extension to convert DioException to AppException
extension DioExceptionExtension on DioException {
  AppException toAppException() {
    switch (type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.receiveTimeout:
        return AppException.network(
          message: 'Connection timeout',
          code: code,
        );
      case DioExceptionType.connectionError:
        return AppException.network(
          message: 'No internet connection',
          code: code,
        );
      case DioExceptionType.badResponse:
        final statusCode = response?.statusCode;
        if (statusCode == 401) {
          return AppException.server(
            message: 'Unauthorized',
            code: statusCode ?? 0,
          );
        }
        if (statusCode == 403) {
          return AppException.server(
            message: 'Forbidden',
            code: statusCode ?? 0,
          );
        }
        if (statusCode == 404) {
          return AppException.server(
            message: 'Resource not found',
            code: statusCode ?? 0,
          );
        }
        if (statusCode != null && statusCode >= 500) {
          return AppException.server(
            message: 'Server error',
            code: statusCode,
          );
        }
        return AppException.server(
          message: message ?? 'Unknown error',
          code: statusCode ?? 0,
          data: response?.data,
        );
      case DioExceptionType.cancel:
        return AppException.network(
          message: 'Request cancelled',
          code: code,
        );
      case DioExceptionType.unknown:
      case DioExceptionType.badCertificate:
      default:
        return AppException.unknown(
          message: message ?? 'Unknown error',
          error: this,
        );
    }
  }
}