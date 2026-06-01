import 'package:sqflite/sqflite.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:path/path.dart';

import '../constants/storage_keys.dart';

/// Database provider
final databaseProvider = Provider<DatabaseService>((ref) {
  return DatabaseService();
});

/// Database service for structured data storage
class DatabaseService {
  Database? _db;

  /// Initialize database
  Future<void> init() async {
    final dbPath = await getDatabasesPath();
    final path = join(dbPath, StorageKeys.databaseName);

    _db = await openDatabase(
      path,
      version: StorageKeys.databaseVersion,
      onCreate: _onCreate,
      onUpgrade: _onUpgrade,
    );
  }

  /// Get database instance
  Database get db {
    if (_db == null) {
      throw StateError('Database not initialized. Call init() first.');
    }
    return _db!;
  }

  /// Create tables
  Future<void> _onCreate(Database db, int version) async {
    await db.execute('''
      CREATE TABLE devices (
        id TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        type TEXT,
        status TEXT,
        last_connected_at INTEGER,
        created_at INTEGER,
        updated_at INTEGER
      )
    ''');

    await db.execute('''
      CREATE TABLE audio_records (
        id TEXT PRIMARY KEY,
        file_path TEXT NOT NULL,
        duration INTEGER,
        size INTEGER,
        created_at INTEGER
      )
    ''');

    await db.execute('''
      CREATE TABLE cache (
        key TEXT PRIMARY KEY,
        data TEXT NOT NULL,
        created_at INTEGER,
        expires_at INTEGER
      )
    ''');
  }

  /// Upgrade database
  Future<void> _onUpgrade(Database db, int oldVersion, int newVersion) async {
    // Handle migrations here
  }

  /// Insert data
  Future<int> insert(String table, Map<String, dynamic> data) async {
    return await db.insert(table, data);
  }

  /// Update data
  Future<int> update(
    String table,
    Map<String, dynamic> data,
    String where,
    List<dynamic> whereArgs,
  ) async {
    return await db.update(table, data, where: where, whereArgs: whereArgs);
  }

  /// Delete data
  Future<int> delete(String table, String where, List<dynamic> whereArgs) async {
    return await db.delete(table, where: where, whereArgs: whereArgs);
  }

  /// Query data
  Future<List<Map<String, dynamic>>> query(
    String table, {
    String? where,
    List<dynamic>? whereArgs,
    String? orderBy,
    int? limit,
    int? offset,
  }) async {
    return await db.query(
      table,
      where: where,
      whereArgs: whereArgs,
      orderBy: orderBy,
      limit: limit,
      offset: offset,
    );
  }

  /// Get single row
  Future<Map<String, dynamic>?> getSingle(
    String table,
    String where,
    List<dynamic> whereArgs,
  ) async {
    final results = await query(table, where: where, whereArgs: whereArgs, limit: 1);
    return results.isNotEmpty ? results.first : null;
  }

  /// Execute raw query
  Future<List<Map<String, dynamic>>> rawQuery(String sql) async {
    return await db.rawQuery(sql);
  }

  /// Clear table
  Future<int> clearTable(String table) async {
    return await db.delete(table);
  }

  /// Close database
  Future<void> close() async {
    await db.close();
    _db = null;
  }
}