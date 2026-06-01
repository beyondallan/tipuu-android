import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/core.dart';
import '../entities/audio_entities.dart';
import '../services/audio_service.dart';

/// Start recording use case
class StartRecordingUseCase {
  final AudioService _service;

  StartRecordingUseCase(this._service);

  Future<Result<String>> call({
    String? outputPath,
    int? sampleRate,
    int? channels,
  }) {
    return _service.startRecording(
      outputPath: outputPath,
      sampleRate: sampleRate,
      channels: channels,
    );
  }
}

/// Stop recording use case
class StopRecordingUseCase {
  final AudioService _service;

  StopRecordingUseCase(this._service);

  Future<Result<AudioRecord>> call() {
    return _service.stopRecording();
  }
}

/// Play audio use case
class PlayAudioUseCase {
  final AudioService _service;

  PlayAudioUseCase(this._service);

  Future<Result<void>> call(String filePath) {
    return _service.play(filePath);
  }
}

/// Start audio call use case
class StartAudioCallUseCase {
  final AudioCallService _service;

  StartAudioCallUseCase(this._service);

  Future<Result<void>> call({
    required String serverUrl,
    required String roomId,
    String? token,
  }) {
    return _service.startCall(
      serverUrl: serverUrl,
      roomId: roomId,
      token: token,
    );
  }
}

/// End audio call use case
class EndAudioCallUseCase {
  final AudioCallService _service;

  EndAudioCallUseCase(this._service);

  Future<Result<void>> call() {
    return _service.endCall();
  }
}

/// Use case providers
final startRecordingUseCaseProvider = Provider<StartRecordingUseCase>((ref) {
  return StartRecordingUseCase(ref.watch(audioServiceProvider));
});

final stopRecordingUseCaseProvider = Provider<StopRecordingUseCase>((ref) {
  return StopRecordingUseCase(ref.watch(audioServiceProvider));
});

final playAudioUseCaseProvider = Provider<PlayAudioUseCase>((ref) {
  return PlayAudioUseCase(ref.watch(audioServiceProvider));
});

final startAudioCallUseCaseProvider = Provider<StartAudioCallUseCase>((ref) {
  return StartAudioCallUseCase(ref.watch(audioCallServiceProvider));
});

final endAudioCallUseCaseProvider = Provider<EndAudioCallUseCase>((ref) {
  return EndAudioCallUseCase(ref.watch(audioCallServiceProvider));
});