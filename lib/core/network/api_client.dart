import 'package:dio/dio.dart';

import 'api_result.dart';
import '../errors/app_exception.dart';

/// Base API client with common methods
class ApiClient {
  final Dio _dio;

  ApiClient(this._dio);

  /// GET request
  Future<ApiResult<T>> get<T>(
    String path, {
    Map<String, dynamic>? queryParameters,
    Options? options,
    T Function(dynamic data)? parser,
  }) async {
    try {
      final response = await _dio.get(
        path,
        queryParameters: queryParameters,
        options: options,
      );
      final data = parser != null ? parser(response.data) : response.data as T;
      return ApiResult.success(data);
    } on DioException catch (e) {
      return ApiResult.failure(e.toAppException());
    }
  }

  /// POST request
  Future<ApiResult<T>> post<T>(
    String path, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
    Options? options,
    T Function(dynamic data)? parser,
  }) async {
    try {
      final response = await _dio.post(
        path,
        data: data,
        queryParameters: queryParameters,
        options: options,
      );
      final result = parser != null ? parser(response.data) : response.data as T;
      return ApiResult.success(result);
    } on DioException catch (e) {
      return ApiResult.failure(e.toAppException());
    }
  }

  /// PUT request
  Future<ApiResult<T>> put<T>(
    String path, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
    Options? options,
    T Function(dynamic data)? parser,
  }) async {
    try {
      final response = await _dio.put(
        path,
        data: data,
        queryParameters: queryParameters,
        options: options,
      );
      final result = parser != null ? parser(response.data) : response.data as T;
      return ApiResult.success(result);
    } on DioException catch (e) {
      return ApiResult.failure(e.toAppException());
    }
  }

  /// DELETE request
  Future<ApiResult<T>> delete<T>(
    String path, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
    Options? options,
    T Function(dynamic data)? parser,
  }) async {
    try {
      final response = await _dio.delete(
        path,
        data: data,
        queryParameters: queryParameters,
        options: options,
      );
      final result = parser != null ? parser(response.data) : response.data as T;
      return ApiResult.success(result);
    } on DioException catch (e) {
      return ApiResult.failure(e.toAppException());
    }
  }
}