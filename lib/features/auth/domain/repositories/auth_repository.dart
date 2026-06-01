import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../core/errors/errors.dart';
import '../entities/user.dart';

/// Auth repository interface
abstract class AuthRepository {
  /// Login with username and password
  Future<Result<User>> login(String username, String password);

  /// Logout current user
  Future<Result<void>> logout();

  /// Get current user
  Future<Result<User?>> getCurrentUser();

  /// Check if user is logged in
  Future<bool> isLoggedIn();

  /// Register new user
  Future<Result<User>> register({
    required String username,
    required String email,
    required String password,
  });

  /// Refresh token
  Future<Result<void>> refreshToken();

  /// Update user profile
  Future<Result<User>> updateProfile(User user);
}

/// Auth repository provider
final authRepositoryProvider = Provider<AuthRepository>((ref) {
  throw UnimplementedError('authRepositoryProvider must be overridden');
});