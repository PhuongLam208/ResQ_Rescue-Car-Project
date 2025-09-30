import 'dart:convert';
import 'package:http/http.dart' as http;
import './api_result.dart';
import '../config/app_config.dart';

class VerifyService {
  // static const String baseUrl = 'http://192.168.1.100:9090/api/resq/verify';

  Future<ApiResult> sendOtp(String phoneNumber) async {
    final response = await http.post(
      Uri.parse('$verifyUrl/send-otp?phoneNumber=$phoneNumber'),
    );
    return ApiResult(response.statusCode, response.body);
  }

  Future<ApiResult> verifyOtp(
    String phoneNumber,
    String code,
    String otpType,
  ) async {
    final response = await http.post(
      Uri.parse('$verifyUrl/check-otp'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'phoneNumber': phoneNumber,
        'code': code,
        'otpType': otpType,
      }),
    );
    return ApiResult(response.statusCode, response.body);
  }

  Future<ApiResult> forgetPassword(String phoneNumber) async {
    final response = await http.post(
      Uri.parse('$verifyUrl/forget-password?phoneNumber=$phoneNumber'),
    );
    return ApiResult(response.statusCode, response.body);
  }

  Future<ApiResult> updatePhoneNumber(int userId, String phoneNumber) async {
    final response = await http.post(
      Uri.parse('$verifyUrl/update-phone/$userId?phoneNumber=$phoneNumber'),
    );
    return ApiResult(response.statusCode, response.body);
  }
}
