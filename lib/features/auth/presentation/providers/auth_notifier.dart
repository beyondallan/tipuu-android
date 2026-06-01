import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../domain/entities/user.dart';
import '../../domain/usecases/auth_usecases.dart';
import 'auth_state.dart';

/// Auth state notifier
class AuthNotifier extends StateNotifier<AuthState> {
  final LoginUseCase _loginUseCase;
  final LogoutUseCase _logoutUseCase;
  final GetCurrentUserUseCase _getCurrentUserUseCase;

  AuthNotifier(
    this._loginUseCase,
    this._logoutUseCase,
    this._getCurrentUserUseCase,
  ) : super(const AuthState.initial()) {
    _checkAuthStatus();
  }

  /// Check authentication status on init
  Future<void> _checkAuthStatus() async {
    state = const AuthState.loading();
    final result = await _getCurrentUserUseCase();
    result.when(
      success: (user) {
        if (user != null) {
          state = AuthState.authenticated(user);
        } else {
          state = const AuthState.unauthenticated();
        }
      },
      failure: (e) => state = const AuthState.unauthenticated(),
    );
  }

  /// Login
  Future<void> login(String username, String password) async {
    state = const AuthState.loading();
    final result = await _loginUseCase(username, password);
    result.when(
      success: (user) => state = AuthState.authenticated(user),
      failure: (e) => state = AuthState.error(e.message),
    );
  }

  /// Logout
  Future<void> logout() async {
    state = const AuthState.loading();
    final result = await _logoutUseCase();
    result.when(
      success: () => state = const AuthState.unauthenticated(),
      failure: (e) => state = AuthState.error(e.message),
    );
  }

  /// Reset state
  void reset() {
    state = const AuthState.initial();
  }
}

/// Auth notifier provider
final authNotifierProvider = StateNotifierProvider<AuthNotifier, AuthState>(
  (ref) => AuthNotifier(
    ref.watch(loginUseCaseProvider),
    ref.watch(logoutUseCaseProvider),
    ref.watch(getCurrentUserUseCaseProvider),
  ),
);

/// Check if user is authenticated
final isAuthenticatedProvider = Provider<bool>((ref) {
  final state = ref.watch(authNotifierProvider);
  return state is AuthAuthenticated;
});

/// Get current user
final currentUserProvider = Provider<User?>((ref) {
  final state = ref.watch(authNotifierProvider);
  return state is AuthAuthenticated ? state.user : null;
});