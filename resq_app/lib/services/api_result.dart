import 'dart:convert';

// Wrapper class để trả về cả statusCode và nội dung body
class ApiResult {
  final int statusCode;
  final String body;

  ApiResult(this.statusCode, this.body);

  bool get isSuccess => statusCode >= 200 && statusCode < 300;

  // Nếu server trả về JSON kiểu { "message": "...", ... }
  String get message {
    try {
      final json = jsonDecode(body);
      return json['message'] ?? body;
    } catch (_) {
      return body; // fallback nếu không phải JSON
    }
  }
}