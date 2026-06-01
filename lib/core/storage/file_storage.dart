import 'package:path_provider/path_provider.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'dart:io';

/// File storage service provider
final fileStorageProvider = Provider<FileStorageService>((ref) {
  return FileStorageService();
});

/// File storage service for media files and large data
class FileStorageService {
  late final Directory _appDir;
  late final Directory _cacheDir;
  late final Directory _audioDir;

  FileStorageService();

  /// Initialize directories
  Future<void> init() async {
    _appDir = await getApplicationDocumentsDirectory();
    _cacheDir = await getTemporaryDirectory();
    _audioDir = Directory('${_appDir.path}/audio');

    if (!_audioDir.existsSync()) {
      _audioDir.createSync(recursive: true);
    }
  }

  /// Get audio directory
  Directory get audioDirectory => _audioDir;

  /// Get cache directory
  Directory get cacheDirectory => _cacheDir;

  /// Get app directory
  Directory get appDirectory => _appDir;

  /// Save audio file
  Future<File> saveAudioFile(String filename, List<int> bytes) async {
    final file = File('${_audioDir.path}/$filename');
    await file.writeAsBytes(bytes);
    return file;
  }

  /// Get audio file
  File? getAudioFile(String filename) {
    final file = File('${_audioDir.path}/$filename');
    return file.existsSync() ? file : null;
  }

  /// Delete audio file
  Future<bool> deleteAudioFile(String filename) async {
    final file = File('${_audioDir.path}/$filename');
    if (file.existsSync()) {
      await file.delete();
      return true;
    }
    return false;
  }

  /// List audio files
  List<File> listAudioFiles() {
    return _audioDir
        .listSync()
        .whereType<File>()
        .toList();
  }

  /// Get total audio storage size
  int getAudioStorageSize() {
    return listAudioFiles()
        .fold(0, (total, file) => total + file.lengthSync());
  }

  /// Clear cache directory
  Future<void> clearCache() async {
    if (_cacheDir.existsSync()) {
      await _cacheDir.delete(recursive: true);
      await _cacheDir.create();
    }
  }

  /// Save file to custom path
  Future<File> saveFile(String path, List<int> bytes) async {
    final file = File('${_appDir.path}/$path');
    if (!file.parent.existsSync()) {
      file.parent.createSync(recursive: true);
    }
    await file.writeAsBytes(bytes);
    return file;
  }

  /// Read file
  File? readFile(String path) {
    final file = File('${_appDir.path}/$path');
    return file.existsSync() ? file : null;
  }
}