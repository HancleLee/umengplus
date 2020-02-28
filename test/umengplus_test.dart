import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:umengplus/umengplus.dart';

void main() {
  const MethodChannel channel = MethodChannel('umengplus');

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
    expect(await Umengplus.platformVersion, '42');
  });
}
