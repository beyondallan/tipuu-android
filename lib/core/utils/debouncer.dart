import 'dart:async';

/// Debounce helper for delaying function calls
class Debouncer {
  final Duration delay;
  Timer? _timer;

  Debouncer({required this.delay});

  void call(Function action) {
    _timer?.cancel();
    _timer = Timer(delay, action);
  }

  void dispose() {
    _timer?.cancel();
  }
}

/// Throttle helper for limiting function calls
class Throttler {
  final Duration duration;
  bool _isReady = true;
  Timer? _timer;

  Throttler({required this.duration});

  void call(Function action) {
    if (!_isReady) return;
    _isReady = false;
    action();
    _timer = Timer(duration, () {
      _isReady = true;
    });
  }

  void dispose() {
    _timer?.cancel();
  }
}