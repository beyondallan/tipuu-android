import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Secure storage provider for sensitive data (tokens, keys)
final secureStorageProvider = Provider<SecureStorageService>((ref) {
  return SecureStorageService();
});

/// Secure storage service
class SecureStorageService {
  final FlutterSecureStorage _storage;

  SecureStorageService({
    FlutterSecureStorage? storage,
  }) : _storage = storage ?? const FlutterSecureStorage(
    aOptions: AndroidOptions(
      encryptedSharedPreferences: true,
    ),
  );

  /// Read value
  Future<String?> read(String key) async {
    return await _storage.read(key: key);
  }

  /// Write value
  Future<void> write(String key, String value) async {
    await _storage.write(key: key, value: value);
  }

  /// Delete value
  Future<void> delete(String key) async {
    await _storage.delete(key: key);
  }

  /// Delete all
  Future<void> deleteAll() async {
    await _storage.deleteAll();
  }

  /// Read all
  Future<Map<String, String>> readAll() async {
    return await _storage.readAll();
  }

  /// Check if contains key
  Future<bool> containsKey(String key) async {
    return await _storage.containsKey(key: key);
  }
}