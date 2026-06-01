/// Storage keys constants
class StorageKeys {
  StorageKeys._();

  // Auth
  static const String accessToken = 'access_token';
  static const String refreshToken = 'refresh_token';
  static const String userId = 'user_id';

  // User
  static const String userProfile = 'user_profile';
  static const String userSettings = 'user_settings';

  // Cache
  static const String cachePrefix = 'cache_';
  static const String lastSyncTime = 'last_sync_time';

  // Database
  static const String databaseName = 'tipuu.db';
  static const int databaseVersion = 1;
}