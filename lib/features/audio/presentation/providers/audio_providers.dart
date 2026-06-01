import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../domain/entities/audio_entities.dart';
import '../domain/services/audio_service.dart';
import '../domain/usecases/audio_usecases.dart';

/// Audio recorder notifier
class AudioRecorderNotifier extends StateNotifier<RecorderState> {
  final AudioService _service;
  final StartRecordingUseCase _startUseCase;
  final StopRecordingUseCase _stopUseCase;

  AudioRecorderNotifier(
    this._service,
    this._startUseCase,
    this._stopUseCase,
  ) : super(RecorderState.idle);

  Future<void> startRecording() async {
    final result = await _startUseCase();
    result.when(
      success: (_) => state = RecorderState.recording,
      failure: (_) => state = RecorderState.error,
    );
  }

  Future<AudioRecord?> stopRecording() async {
    state = RecorderState.idle;
    final result = await _stopUseCase();
    return result.dataOrNull;
  }

  Future<void> pauseRecording() async {
    await _service.pauseRecording();
    state = RecorderState.paused;
  }

  Future<void> resumeRecording() async {
    await _service.resumeRecording();
    state = RecorderState.recording;
  }
}

/// Audio recorder provider
final audioRecorderProvider =
    StateNotifierProvider<AudioRecorderNotifier, RecorderState>((ref) {
  return AudioRecorderNotifier(
    ref.watch(audioServiceProvider),
    ref.watch(startRecordingUseCaseProvider),
    ref.watch(stopRecordingUseCaseProvider),
  );
});

/// Recording duration stream
final recordingDurationProvider = StreamProvider<int>((ref) {
  return ref.watch(audioServiceProvider).recordingDuration;
});

/// Audio call notifier
class AudioCallNotifier extends StateNotifier<AudioCallState> {
  final AudioCallService _service;
  final StartAudioCallUseCase _startUseCase;
  final EndAudioCallUseCase _endUseCase;

  AudioCallNotifier(
    this._service,
    this._startUseCase,
    this._endUseCase,
  ) : super(const AudioCallState.idle()) {
    _service.callState.listen((state) {
      this.state = state;
    });
  }

  Future<void> startCall({
    required String serverUrl,
    required String roomId,
    String? token,
  }) async {
    state = const AudioCallState.connecting();
    final result = await _startUseCase(
      serverUrl: serverUrl,
      roomId: roomId,
      token: token,
    );
    result.when(
      success: () {},
      failure: (e) => state = AudioCallState.error(e.message),
    );
  }

  Future<void> endCall() async {
    await _endUseCase();
    state = const AudioCallState.idle();
  }

  Future<void> toggleMute() async {
    if (_service.isMuted()) {
      await _service.unmute();
    } else {
      await _service.mute();
    }
  }
}

/// Audio call provider
final audioCallProvider =
    StateNotifierProvider<AudioCallNotifier, AudioCallState>((ref) {
  return AudioCallNotifier(
    ref.watch(audioCallServiceProvider),
    ref.watch(startAudioCallUseCaseProvider),
    ref.watch(endAudioCallUseCaseProvider),
  );
});

/// Audio records list provider
final audioRecordsProvider = StateNotifierProvider<AudioRecordsNotifier, List<AudioRecord>>((ref) {
  return AudioRecordsNotifier();
});

class AudioRecordsNotifier extends StateNotifier<List<AudioRecord>> {
  AudioRecordsNotifier() : super([]);

  void addRecord(AudioRecord record) {
    state = [...state, record];
  }

  void removeRecord(String id) {
    state = state.where((r) => r.id != id).toList();
  }
}