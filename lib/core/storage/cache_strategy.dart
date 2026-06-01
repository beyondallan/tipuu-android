/// Cache strategy for data fetching
enum CacheStrategy {
  /// Return cached data if available, otherwise fetch from network
  cacheFirst,

  /// Fetch from network first, fallback to cache on failure
  networkFirst,

  /// Return cached data immediately, then fetch fresh data from network
  staleWhileRevalidate,

  /// Only fetch from network, no caching
  networkOnly,

  /// Only return cached data, no network request
  cacheOnly,
}