import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'app.dart';
import 'core/core.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Initialize services
  final sharedPreferences = await SharedPreferences.getInstance();
  final fileStorage = FileStorageService();
  await fileStorage.init();

  final database = DatabaseService();
  await database.init();

  runApp(
    ProviderScope(
      overrides: [
        sharedPreferencesProvider.overrideWithValue(sharedPreferences),
        fileStorageProvider.overrideWithValue(fileStorage),
        databaseProvider.overrideWithValue(database),
      ],
      child: const App(),
    ),
  );
}