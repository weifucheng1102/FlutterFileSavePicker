import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:file_save_picker/file_save_picker.dart';

void main() {
  const MethodChannel channel = MethodChannel('file_save_picker');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await FileSavePicker.platformVersion, '42');
  });
}
