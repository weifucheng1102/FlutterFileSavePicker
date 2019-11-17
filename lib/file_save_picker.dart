import 'dart:async';

import 'dart:typed_data';
import 'package:flutter/services.dart';

class FileSavePicker {
  static const MethodChannel _channel = const MethodChannel('file_save_picker');
  static const String _tag = 'FileSavePicker';

  FileSavePicker._();

  /// Returns an absolute file path from the calling platform.
  static Future<dynamic> saveFile(
      {Uint8List bytes, String mimeType, String filename}) async {
    try {
      final String path = await _channel.invokeMethod('save_file', {
        'bytes': bytes, 'mimeType': mimeType, 'filename': filename
      });

      return path;
    } on PlatformException catch (e) {
      print('[$_tag] Platform exception: $e');
      rethrow;
    } catch (e) {
      print(
          '[$_tag] Unsupported operation. Method not found. The exception thrown was: $e');
      rethrow;
    }
  }
}
