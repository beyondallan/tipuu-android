import 'package:uuid/uuid.dart';

/// Unique ID generator
final uuidGenerator = Uuid();

/// Generate unique ID
String generateId() => uuidGenerator.v4();

/// Generate short unique ID
String generateShortId() => uuidGenerator.v4().substring(0, 8);