import 'package:freezed_annotation/freezed_annotation.dart';

part 'audio_record.freezed.dart';
part 'audio_record.g.dart';

/// Audio record entity
@freezed
class AudioRecord with _$AudioRecord {
  const factory AudioRecord({
    required String id,
    required String filePath,
    required int durationMs,
    required int sizeBytes,
    String? title,
    @JsonKey(name: 'created_at') required DateTime createdAt,
  }) = _AudioRecord;

  factory AudioRecord.fromJson(Map<String, dynamic> json) => _$AudioRecordFromJson(json);
}

/// Audio call state
@freezed
class AudioCallState with _$AudioCallState {
  const factory AudioCallState.idle() = AudioCallIdle;
  const factory AudioCallState.connecting() = AudioCallConnecting;
  const factory AudioCallState.active({
    required String roomId,
    required List<String> participants,
  }) = AudioCallActive;
  const factory AudioCallState.error(String message) = AudioCallError;
}