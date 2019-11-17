# file_save_picker
Android only. Unless someone wants to buy me some apple stuff.

  file_save_picker:
    path: ../file_save_picker
    
import 'package:file_save_picker/file_save_picker.dart';

    var filePath = await FileSavePicker.saveFile(
        bytes: fileData.bodyBytes,
        mimeType: "image/png",
        filename: '1493105660290.jpg');

## Getting Started

This project is a starting point for a Flutter
[plug-in package](https://flutter.dev/developing-packages/),
a specialized package that includes platform-specific implementation code for
Android and/or iOS.

For help getting started with Flutter, view our
[online documentation](https://flutter.dev/docs), which offers tutorials,
samples, guidance on mobile development, and a full API reference.
