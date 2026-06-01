import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/core.dart';
import '../entities/audio_entities.dart';

/// Audio recorder state
enum RecorderState {
  idle,
  recording,
  paused,
  error,
}

/// Audio service interface
abstract class AudioService {
  /// Current recorder state
  Stream<RecorderState> get recorderState;

  /// Check if microphone is available
  Future<bool> isMicrophoneAvailable();

  /// Start recording
  Future<Result<String>> startRecording({
    String? outputPath,
    int? sampleRate,
    int? channels,
  });

  /// Stop recording and get file path
  Future<Result<AudioRecord>> stopRecording();

  /// Pause recording
  Future<Result<void>> pauseRecording();

  /// Resume recording
  Future<Result<void>> resumeRecording();

  /// Get recording duration stream
  Stream<int> get recordingDuration;

  /// Play audio file
  Future<Result<void>> play(String filePath);

  /// Stop playback
  Future<Result<void>> stopPlayback();

  /// Pause playback
  Future<Result<void>> pausePlayback();

  /// Resume playback
  Future<Result<void>> resumePlayback();

  /// Get playback position stream
  Stream<int> get playbackPosition;

  /// Get playback duration
  Future<int> getPlaybackDuration(String filePath);
}

/// Audio call service interface
abstract class AudioCallService {
  /// Current call state
  Stream<AudioCallState> get callState;

  /// Start audio call
  Future<Result<void>> startCall({
    required String serverUrl,
    required String roomId,
    String? token,
  });

  /// End audio call
  Future<Result<void>> endCall();

  /// Mute microphone
  Future<Result<void>> mute();

  /// Unmute microphone
  Future<Result<void>> unmute();

  /// Check if muted
  bool isMuted();

  /// Adjust volume
  Future<Result<void>> setVolume(double volume);
}

/// Audio service provider
final audioServiceProvider = Provider<AudioService>((ref) {
  throw UnimplementedError('audioServiceProvider must be overridden');
});

/// Audio call service provider
final audioCallServiceProvider = Provider<AudioCallService>((ref) {
  throw UnimplementedError('audioCallServiceProvider must be overridden');
});