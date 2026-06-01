import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/auth/presentation/pages/login_page.dart';
import '../../features/auth/presentation/pages/splash_page.dart';
import '../../features/home/presentation/pages/home_page.dart';

/// Router provider
final routerProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    initialLocation: '/splash',
    routes: [
      GoRoute(
        path: '/splash',
        name: 'splash',
        builder: (context, state) => const SplashPage(),
      ),
      GoRoute(
        path: '/login',
        name: 'login',
        builder: (context, state) => const LoginPage(),
      ),
      GoRoute(
        path: '/home',
        name: 'home',
        builder: (context, state) => const HomePage(),
      ),
      // Feature routes will be added here
      // Bluetooth routes
      GoRoute(
        path: '/bluetooth',
        name: 'bluetooth',
        builder: (context, state) => const PlaceholderPage(title: 'Bluetooth'),
      ),
      // Audio routes
      GoRoute(
        path: '/audio',
        name: 'audio',
        builder: (context, state) => const PlaceholderPage(title: 'Audio'),
      ),
    ],
    errorBuilder: (context, state) => const PlaceholderPage(title: 'Error'),
  );
});

/// Placeholder page for routes not yet implemented
class PlaceholderPage extends StatelessWidget {
  final String title;

  const PlaceholderPage({super.key, required this.title});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: Center(
        child: Text('$title Page - Coming Soon'),
      ),
    );
  }
}