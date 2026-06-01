/// Application configuration constants
class AppConfig {
  AppConfig._();

  // App info
  static const String appName = 'Tipuu';
  static const String appVersion = '1.0.0';

  // API configuration
  static const String apiBaseUrl = 'https://api.tipuu.com';
  static const Duration apiTimeout = Duration(seconds: 30);

  // Storage keys
  static const String tokenKey = 'auth_token';
  static const String userKey = 'user_data';
  static const String themeKey = 'theme_mode';

  // Cache
  static const Duration cacheExpiration = Duration(hours: 24);
  static const int maxCacheSize = 100 * 1024 * 1024; // 100MB
}