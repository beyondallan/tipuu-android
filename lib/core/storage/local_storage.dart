import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Shared preferences provider
final sharedPreferencesProvider = Provider<SharedPreferences>((ref) {
  throw UnimplementedError('sharedPreferencesProvider must be overridden');
});

/// Local storage service for general data
final localStorageProvider = Provider<LocalStorageService>((ref) {
  final prefs = ref.watch(sharedPreferencesProvider);
  return LocalStorageService(prefs);
});

/// Local storage service
class LocalStorageService {
  final SharedPreferences _prefs;

  LocalStorageService(this._prefs);

  /// Get string
  String? getString(String key) => _prefs.getString(key);

  /// Set string
  Future<bool> setString(String key, String value) =>
      _prefs.setString(key, value);

  /// Get int
  int? getInt(String key) => _prefs.getInt(key);

  /// Set int
  Future<bool> setInt(String key, int value) => _prefs.setInt(key, value);

  /// Get bool
  bool? getBool(String key) => _prefs.getBool(key);

  /// Set bool
  Future<bool> setBool(String key, bool value) => _prefs.setBool(key, value);

  /// Get double
  double? getDouble(String key) => _prefs.getDouble(key);

  /// Set double
  Future<bool> setDouble(String key, double value) =>
      _prefs.setDouble(key, value);

  /// Get string list
  List<String>? getStringList(String key) => _prefs.getStringList(key);

  /// Set string list
  Future<bool> setStringList(String key, List<String> value) =>
      _prefs.setStringList(key, value);

  /// Remove key
  Future<bool> remove(String key) => _prefs.remove(key);

  /// Clear all
  Future<bool> clear() => _prefs.clear();

  /// Check if contains key
  bool containsKey(String key) => _prefs.containsKey(key);
}