import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:latlong2/latlong.dart';
import 'package:resq_app/config/app_config.dart';
import 'package:resq_app/models/auth/login_response.dart';
import 'package:resq_app/models/rescue_response.dart';
import 'package:resq_app/models/rescue_service.dart';

class RescueApiService {
  static Future<RescueResponse?> fetchRescueResponse(int rrid) async {
    final url = Uri.parse(
      '$baseUrl/pcrescue//with-bill/${rrid}',
    ); // Thay đổi endpoint nếu cần
    try {
      final response = await http.get(url);

      if (response.statusCode == 200) {
        final Map<String, dynamic> json = jsonDecode(response.body);
        return RescueResponse.fromJson(json);
      } else {
        print('❌ Error fetching data: ${response.statusCode}');
      }
    } catch (e) {
      print('❌ Exception: $e');
    }
    return null; // Trả về null nếu có lỗi
  }

  // Get services by rescue type
  static Future<List<RescueService>> getServicesByType(
    String rescueType,
  ) async {
    final response = await http.get(
      Uri.parse('$partnerUrl/services/$rescueType'),
      headers: headers,
    );

    if (response.statusCode == 200) {
      List<dynamic> data = json.decode(response.body);
      return data.map((service) => RescueService.fromJson(service)).toList();
    } else {
      throw Exception('Failed to load services');
    }
  }

  //Giảm giá
  static Future<List<RescueService>> getCustomerDiscount(int userId) async {
    final response = await http.get(
      Uri.parse('$baseUrl/api/resq/pcrescue/discounts/available/$userId'),
      headers: headers,
    );

    if (response.statusCode == 200) {
      final Map<String, dynamic> decoded = json.decode(response.body);
      final List<dynamic> data = decoded['data'];
      return data.map((item) => RescueService.fromJson(item)).toList();
    } else {
      throw Exception('Failed to load services');
    }
  }

  //Khoảng cách 2 điểm
  static Future<double> getDistanceFromPoints(List<LatLng> points) async {
    if (points.length < 2) return 0.0;

    double totalDistance = 0.0;

    for (int i = 0; i < points.length - 1; i++) {
      final lat1 = points[i].latitude;
      final lon1 = points[i].longitude;
      final lat2 = points[i + 1].latitude;
      final lon2 = points[i + 1].longitude;

      final url =
          '$resqUrl/pcrescue/distance'
          '?lat1=$lat1&lon1=$lon1&lat2=$lat2&lon2=$lon2';

      try {
        final response = await http.get(Uri.parse(url), headers: headers);
        if (response.statusCode == 200) {
          final distance = double.tryParse(response.body);
          if (distance != null) {
            totalDistance += distance;
          } else {
            print('Lỗi: Không đọc được distance từ response: ${response.body}');
          }
        } else {
          print('Lỗi API distance: ${response.statusCode} - ${response.body}');
        }
      } catch (e) {
        print('Lỗi gọi API distance: $e');
      }
    }

    return double.parse(totalDistance.toStringAsFixed(2));
  }

  // Create Rescue Rquest + Bill

  Future<RescueResponse?> createRescueRequest(
    Map<String, dynamic> requestData,
  ) async {
    const url =
        '$resqUrl/pcrescue/rescue/request'; // sửa thành domain backend nếu cần

    try {
      final response = await http.post(
        Uri.parse(url),
        headers: headers,
        body: jsonEncode(requestData),
      );

      if (response.statusCode == 200) {
        final body = jsonDecode(response.body);
        final data = body['data'];
        return RescueResponse.fromJson(data);
      } else {
        print('API error: ${response.statusCode} - ${response.body}');
      }
    } catch (e) {
      print('Exception: $e');
    }

    return null;
  }

  Future<bool> dispatchRescueRequest(int rrid, double lat, double lon) async {
    try {
      final response = await http.post(
        Uri.parse('$resqUrl/pcrescue/dispatch'),
        headers: headers,
        body: jsonEncode({'rrid': rrid, 'userLat': lat, 'userLon': lon}),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return data['status'] == 200;
      }
      return false;
    } catch (e) {
      print('Error dispatching rescue request: $e');
      return false;
    }
  }

  Future<String> cancelRescueRequest(int rrid, {String? note}) async {
    try {
      final url = Uri.parse(
        '$baseUrl/api/rescue/cancel?rrid=$rrid&note=${note ?? ''}',
      );

      final response = await http.post(url, headers: headers);

      if (response.statusCode == 200) {
        final data = json.decode(response.body);
        return data['data'] ?? 'Request cancelled successfully.';
      } else {
        final errorMessage =
            json.decode(response.body)['message'] ?? 'Error occurred.';
        throw Exception(errorMessage); // Ném lỗi để xử lý ở nơi gọi
      }
    } catch (e) {
      print('Error cancelling rescue request: $e');
      return 'An error occurred while cancelling the request.'; // Trả về thông báo lỗi
    }
  }

  // //Trang service
  // static Future<List<dynamic>> getPartnerRescue(int partnerId) async {
  //   print("Login $loginResponse");
  //   String? token =
  //       "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJUaW5hIiwicm9sZSI6IlJPTEVfUEFSVE5FUiIsImlhdCI6MTc1MzE2ODU2NiwiZXhwIjoxNzUzMjU0OTY2fQ.jk70rfQtF5PCvWIGWcK-RrUCvhaRBwoQ8Yz4RMpJzsQ";
  //   final url = Uri.parse('$resqUrl/completed/partner/11');
  //   try {
  //     final response = await http.get(
  //       url,
  //       headers: {
  //         'Accept': 'application/json',
  //         'Authorization': 'Bearer $token',
  //       },
  //     );
  //     if (response.statusCode == 200) {
  //       final decoded = jsonDecode(utf8.decode(response.bodyBytes));
  //       print(decoded);
  //       if (decoded is List) {
  //         return decoded;
  //       } else {
  //         throw Exception('Expected List but got ${decoded.runtimeType}');
  //       }
  //     } else {
  //       throw Exception('Server error: ${response.statusCode}');
  //     }
  //   } catch (e) {
  //     throw Exception('Connection error: $e');
  //   }
  // }
}
