import 'dart:convert';
import 'package:http/http.dart' as http;
import '../services/api_result.dart';
import '../config/app_config.dart';
import 'package:resq_app/models/RRInfoDto.dart';

class PartnerService {
  // Future<RRInfoDto?> fetchNewRescueRequest(int partnerId) async {
  //   final url = Uri.parse('$partnerUrl/get-new-rr/$partnerId');
  //   try {
  //     final response = await http.get(url, headers: headers);
  //     if (response.statusCode == 200) {
  //       final data = jsonDecode(response.body);
  //       return RRInfoDto.fromJson(data);
  //     }
  //   } catch (e) {
  //     print("Error fetching new rescue request: $e");
  //   }
  //   return null;
  // }

  Future<ApiResult> partnerFeedback({
    required int rrid,
    required int customerRate,
    required String rescueDescription,
  }) async {
    final url = Uri.parse('$partnerUrl/feedback/save/$rrid');

    try {
      final response = await http.post(
        url,
        headers: headers,
        body: jsonEncode({
          'CustomerRate': customerRate,
          'RescueDescription': rescueDescription,
        }),
      );

      return ApiResult(response.statusCode, response.body);
    } catch (e) {
      return ApiResult(500, 'Connection error: $e');
    }
  }

  Future<ApiResult> getPaymentAmount(int rrid) async {
    final url = Uri.parse('$partnerUrl/get-payment-amount/$rrid');

    try {
      final response = await http.get(url, headers: headers);
      return ApiResult(response.statusCode, response.body);
    } catch (e) {
      return ApiResult(500, 'Connection error: $e');
    }
  }

  Future<ApiResult> receiveMoney({
    required int partnerId,
    required double totalReceived,
    required String paymentMethod,
  }) async {
    final url = Uri.parse('$partnerUrl/receive-money/$partnerId');

    try {
      final response = await http.put(
        url,
        headers: headers,
        body: jsonEncode({
          'totalReceived': totalReceived.toString(),
          'paymentMethod': paymentMethod,
        }),
      );
      return ApiResult(response.statusCode, response.body);
    } catch (e) {
      return ApiResult(500, 'Connection error: $e');
    }
  }

  // Future<ApiResult> partnerAccept(int rrid) async {
  //   final url = Uri.parse('$partnerUrl/accept/$rrid');
  //   try {
  //     final response = await http.put(url);
  //     return ApiResult(response.statusCode, response.body);
  //   } catch (e) {
  //     return ApiResult(500, 'Connection error: $e');
  //   }
  // }

  // Future<ApiResult> partnerDenied(int rrid) async {
  //   final url = Uri.parse('$partnerUrl/denied/$rrid');
  //   try {
  //     final response = await http.put(url);
  //     return ApiResult(response.statusCode, response.body);
  //   } catch (e) {
  //     return ApiResult(500, 'Connection error: $e');
  //   }
  // }

  // Future<ApiResult> partnerArrived(int rrid) async {
  //   final url = Uri.parse('$partnerUrl/arrived/$rrid');
  //   try {
  //     final response = await http.put(url);
  //     return ApiResult(response.statusCode, response.body);
  //   } catch (e) {
  //     return ApiResult(500, 'Connection error: $e');
  //   }
  // }

  // Future<ApiResult> partnerCancel({
  //   required int rrid,
  //   required int partnerId,
  //   required String cancelNote,
  // }) async {
  //   final url = Uri.parse('$partnerUrl/cancel/$rrid');
  //   try {
  //     final response = await http.put(
  //       url,
  //       headers: headers,
  //       body: jsonEncode({
  //         'partnerId': partnerId.toString(),
  //         'cancelNote': cancelNote,
  //       }),
  //     );
  //     return ApiResult(response.statusCode, response.body);
  //   } catch (e) {
  //     return ApiResult(500, 'Connection error: $e');
  //   }
  // }

  // Future<ApiResult> partnerComplete(int rrid) async {
  //   final url = Uri.parse('$partnerUrl/complete/$rrid');
  //   try {
  //     final response = await http.put(url);
  //     return ApiResult(response.statusCode, response.body);
  //   } catch (e) {
  //     return ApiResult(500, 'Connection error: $e');
  //   }
  // }

  static Future<bool> updateWalletPoint(int partnerId) async {
    final url = Uri.parse('$partnerUrl/updateWalletPoint/$partnerId');
    try {
      final response = await http.put(url, headers: headers);

      if (response.statusCode == 200) {
        print('Updated Successfull');
        return true;
      } else {
        print('Update failed: ${response.statusCode} - ${response.body}');
        return false;
      }
    } catch (e) {
      print('Connection error: $e');
      return false;
    }
  }

  static Future<String?> getPartnerPaypalPayment(int partnerId) async {
    final url = Uri.parse('$partnerUrl/paypalPayment/$partnerId');

    try {
      final response = await http.get(url, headers: headers);

      if (response.statusCode == 200) {
        final data = json.decode(response.body);
        print('Paypal Payment: $data');
        return data; // Assuming you want to return the data
      } else {
        print('Error: ${response.statusCode} - ${response.body}');
        return null; // Handle error appropriately
      }
    } catch (e) {
      print('Connection: $e');
      return null; // Handle connection error
    }
  }
}
