import 'dart:convert';
import 'dart:io';
import 'package:http/http.dart' as http;
import './api_result.dart';
import 'package:resq_app/models/home_profile_dto.dart';
import 'package:resq_app/config/app_config.dart';
import 'package:resq_app/models/auth/login_response.dart';
import 'package:resq_app/models/pd_image_dto.dart';

class CustomerService {
  Future<ApiResult> getHomeProfile(int userId) async {
    final url = Uri.parse('$customerUrl/home-profile/$userId');

    try {
      final response = await http.get(url, headers: headers);
      return ApiResult(response.statusCode, response.body);
    } catch (e) {
      return ApiResult(500, 'Connection error: $e');
    }
  }

  Future<HomeProfileDto?> fetchHomeProfile(int userId) async {
    final result = await getHomeProfile(userId);

    if (result.isSuccess) {
      try {
        final json = jsonDecode(result.body);
        return HomeProfileDto.fromJson(json);
      } catch (e) {
        print('Failed to parse JSON: $e');
        return null;
      }
    } else {
      print('Failed to fetch home profile: ${result.message}');
      return null;
    }
  }

  Future<PDImageDto?> fetchPDImages(int userId) async {
    final url = Uri.parse('$customerUrl/pd-images/$userId');
    try {
      final response = await http.get(url, headers: headers);
      if (response.statusCode == 200) {
        final json = jsonDecode(response.body);
        return PDImageDto.fromJson(json);
      } else {
        print('❌ Failed to fetch PD images: ${response.body}');
        return null;
      }
    } catch (e) {
      print('❌ Error fetching PD images: $e');
      return null;
    }
  }

  Future<ApiResult> changePhoneNumber(int userId, String phoneNumber) async {
    final response = await http.put(
      Uri.parse('$customerUrl/change-phone/$userId?phoneNumber=$phoneNumber'),
      headers: headers,
    );
    return ApiResult(response.statusCode, response.body);
  }

  Future<ApiResult> changePassword({
    required int userId,
    required String oldPassword,
    required String newPassword,
  }) async {
    final url = Uri.parse('$customerUrl/change-password/$userId');

    try {
      final response = await http.put(
        url,
        headers: headers,
        body: jsonEncode({
          'oldPassword': oldPassword,
          'newPassword': newPassword,
        }),
      );

      return ApiResult(response.statusCode, response.body);
    } catch (e) {
      return ApiResult(500, 'Connection error: $e');
    }
  }

  Future<ApiResult> customerFeedback({
    required int rrid,
    required int RescueRate,
    required int PartnerRate,
    required String RescueDescription,
  }) async {
    final url = Uri.parse('$customerUrl/feedback/save/$rrid');

    try {
      final response = await http.post(
        url,
        headers: headers,
        body: jsonEncode({
          'RescueRate': RescueRate,
          'PartnerRate': PartnerRate,
          'RescueDescription': RescueDescription,
        }),
      );

      return ApiResult(response.statusCode, response.body);
    } catch (e) {
      return ApiResult(500, 'Connection error: $e');
    }
  }

  static Future<Map<String, dynamic>> getCustomerProfile(int customerId) async {
    final url = Uri.parse('$customerUrl/$customerId');

    try {
      final response = await http.get(url, headers: headers);
      if (response.statusCode == 200) {
        final decoded = jsonDecode(utf8.decode(response.bodyBytes));
        return decoded;
      } else {
        throw Exception('Server error: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Connection error: $e');
    }
  }

  //Update Customer Info
  static Future<Map<String, dynamic>> updateCustomer(
    int customerId,
    Map<String, dynamic> customerDto,
  ) async {
    String? token = loginResponse?.token;
    final url = Uri.parse('$customerUrl/updateCustomer/$customerId');
    try {
      final response = await http.put(
        url,
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
        body: jsonEncode(customerDto),
      );

      final decoded = jsonDecode(utf8.decode(response.bodyBytes));

      if (response.statusCode == 200) {
        return decoded; // Trả về luôn, trong đó có thể chứa success, errors...
      } else {
        // Nếu statusCode != 200, nhưng vẫn có JSON chứa errors
        return {
          "success": false,
          "errors":
              decoded['errors'] ??
              {"general": decoded['message'] ?? "Unknown error"},
        };
      }
    } catch (e) {
      return {
        "success": false,
        "errors": {"general": "Connection error: $e"},
      };
    }
  }

  ///Vehicle///
  // Get Customer Vehicle
  static Future<List<dynamic>> getCustomerVehicles(int customerId) async {
    final url = Uri.parse('$customerUrl/vehicles/$customerId');
    try {
      final response = await http.get(url, headers: headers);
      if (response.statusCode == 200) {
        final decoded = jsonDecode(utf8.decode(response.bodyBytes));
        if (decoded is List) {
          return decoded;
        } else {
          throw Exception('Expected List but got ${decoded.runtimeType}');
        }
      } else {
        throw Exception('Server error: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Connection error: $e');
    }
  }

  static Future<List<dynamic>> getCustomerVehiclesNoDocs(int customerId) async {
    final url = Uri.parse('$customerUrl/vehicles/noDoc/$customerId');
    try {
      final response = await http.get(url, headers: headers);
      if (response.statusCode == 200) {
        final decoded = jsonDecode(utf8.decode(response.bodyBytes));
        if (decoded is List) {
          return decoded;
        } else {
          throw Exception('Expected List but got ${decoded.runtimeType}');
        }
      } else {
        throw Exception('Server error: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Connection error: $e');
    }
  }

  //Create New Vehicle
  static Future<Map<String, dynamic>> createVehicle({
    required int customerId,
    required String plateNo,
    required String brand,
    required String model,
    required int year,
    required File? frontImage,
    required File? backImage,
  }) async {
    String? token = loginResponse?.token;
    final uri = Uri.parse('$customerUrl/vehicles/createNew');
    final vehicleDto = {
      "userId": customerId,
      "plateNo": plateNo,
      "brand": brand,
      "model": model,
      "year": year,
    };
    final vehicleDtoString = jsonEncode(vehicleDto);
    final request =
        http.MultipartRequest('POST', uri)
          ..fields['vehicleDtoString'] = vehicleDtoString
          ..headers['Authorization'] = 'Bearer $token';

    if (frontImage != null) {
      request.files.add(
        await http.MultipartFile.fromPath('frontImage', frontImage.path),
      );
    }
    if (backImage != null) {
      request.files.add(
        await http.MultipartFile.fromPath('backImage', backImage.path),
      );
    }
    final response = await request.send();
    final respStr = await response.stream.bytesToString();

    Map<String, dynamic> parsedBody = {};
    try {
      parsedBody = jsonDecode(respStr);
    } catch (_) {
      // Nếu không decode được thì để parsedBody rỗng
    }
    return {
      "status": response.statusCode,
      "success": response.statusCode == 200,
      "body": respStr,
      "errors": parsedBody["errors"] is Map ? parsedBody["errors"] : {},
      // đảm bảo là Map
    };
  }

  //Update Vehicle
  static Future<Map<String, dynamic>> updateVehicle({
    required int vehicleId,
    required int userId,
    required String plateNo,
    required String brand,
    required String model,
    required int year,
    File? frontImage,
    File? backImage,
  }) async {
    String? token = loginResponse?.token;
    final uri = Uri.parse('$customerUrl/vehicles/updateVehicle/$vehicleId');

    // Tạo MultipartRequest với method PUT
    final request = http.MultipartRequest('PUT', uri);

    // Dữ liệu JSON dưới dạng chuỗi
    final vehicleDto = {
      "vehicleId": vehicleId,
      "userId": userId,
      "plateNo": plateNo,
      "brand": brand,
      "model": model,
      "year": year,
    };
    final vehicleDtoString = jsonEncode(vehicleDto);
    request.fields['vehicleDtoString'] = vehicleDtoString;

    print(vehicleDto);

    // Gửi ảnh nếu có
    if (frontImage != null) {
      request.files.add(
        await http.MultipartFile.fromPath('frontImage', frontImage.path),
      );
    }

    if (backImage != null) {
      request.files.add(
        await http.MultipartFile.fromPath('backImage', backImage.path),
      );
    }

    request.headers.addAll({
      'Accept': 'application/json',
      'Authorization': 'Bearer $token',
    });

    try {
      final streamedResponse = await request.send();
      final respStr = await streamedResponse.stream.bytesToString();
      Map<String, dynamic> parsedBody = {};
      try {
        parsedBody = jsonDecode(respStr);
      } catch (_) {
        // Không parse được thì parsedBody sẽ rỗng
      }

      return {
        "status": streamedResponse.statusCode,
        "success": streamedResponse.statusCode == 200,
        "body": respStr,
        "errors": parsedBody["errors"] is Map ? parsedBody["errors"] : {},
      };
    } catch (e) {
      return {
        "success": false,
        "errors": {"network": "Failed to connect to server: $e"},
      };
    }
  }

  //Delete Vehicle
  static Future<void> deleteVehicle(int vehicleId) async {
    final url = Uri.parse('$customerUrl/vehicles/$vehicleId');
    try {
      final response = await http.delete(url, headers: headers);

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        print("Delete sucess: ${data['message']}");
      } else if (response.statusCode == 204) {
        print("Delete sucess (no content)");
      } else {
        print("Delete fail: ${response.statusCode}");
        print("Fail: ${response.body}");
      }
    } catch (e) {
      print("⚠️ Call API error: $e");
    }
  }

  ///Personal Data///
  //Get Personal Data
  static Future<Map<String, dynamic>> getCustomerPersonalData(
    int customerId,
  ) async {
    final url = Uri.parse('$customerUrl/personaldata/$customerId');
    try {
      final response = await http.get(url, headers: headers);

      if (response.statusCode != 200) {
        throw HttpException(
          'Failed to load data: ${response.statusCode} ${response.reasonPhrase}',
        );
      }

      final decodedBody = utf8.decode(response.bodyBytes);
      final parsedJson = jsonDecode(decodedBody);
      if (parsedJson is Map<String, dynamic>) {
        return parsedJson;
      } else {
        throw FormatException(
          'Expected JSON object but received ${parsedJson.runtimeType}',
        );
      }
    } catch (error) {
      throw Exception('Error fetching personal data: $error');
    }
  }

  //Create New Personal Data
  static Future<Map<String, dynamic>> createPersonalData({
    required int customerId,
    required Map<String, dynamic> personalDataDto,
    required File? frontImage,
    required File? backImage,
    required File? faceImage,
  }) async {
    String? token = loginResponse?.token;
    final uri = Uri.parse('$customerUrl/personaldata/createNew');
    try {
      final personalDataDtoString = jsonEncode(personalDataDto);
      final request =
          http.MultipartRequest('POST', uri)
            ..fields['personalDataString'] = personalDataDtoString
            ..fields['userId'] = customerId.toString()
            ..headers.addAll({
              'Content-Type': 'multipart/form-data',
              'Authorization': 'Bearer $token',
            });

      if (frontImage != null) {
        request.files.add(
          await http.MultipartFile.fromPath('frontImage', frontImage.path),
        );
      }
      if (backImage != null) {
        request.files.add(
          await http.MultipartFile.fromPath('backImage', backImage.path),
        );
      }
      if (faceImage != null) {
        request.files.add(
          await http.MultipartFile.fromPath('faceImage', faceImage.path),
        );
      }
      final response = await request.send();
      final respStr = await response.stream.bytesToString();
      Map<String, dynamic> parsedBody = {};
      try {
        parsedBody = jsonDecode(respStr);
      } catch (_) {
        // Nếu không decode được thì để parsedBody rỗng
      }
      return {
        "status": response.statusCode,
        "success": response.statusCode == 200,
        "body": respStr,
        "errors": parsedBody["errors"] is Map ? parsedBody["errors"] : {},
      };
    } catch (e) {
      throw Exception('Connection error: $e');
    }
  }

  //Update Personal Data
  static Future<Map<String, dynamic>> updatePersonalData({
    required int pdId,
    required Map<String, dynamic> personalDataDto,
    required dynamic frontImage,
    required dynamic backImage,
    required dynamic faceImage,
  }) async {
    String? token = loginResponse?.token;
    final uri = Uri.parse('$customerUrl/personaldata/updatePd/${pdId}');
    try {
      print("PD ID $pdId");
      print("Dto $personalDataDto");
      print("Front $frontImage");
      print("Back $backImage");
      print("Face $faceImage");
      final personalDataDtoString = jsonEncode(personalDataDto);
      final request =
          http.MultipartRequest('PUT', uri)
            ..fields['pdId'] = pdId.toString()
            ..fields['personalDataDtoString'] = personalDataDtoString
            ..headers.addAll({
              'Accept': 'application/json',
              'Authorization': 'Bearer $token',
            });

      if (frontImage != null) {
        if (frontImage is File) {
          request.files.add(
            await http.MultipartFile.fromPath('frontImage', frontImage.path),
          );
        } else if (frontImage is String) {
          request.fields['frontImageUrl'] = frontImage; // URL cũ
        }
      }
      if (backImage != null) {
        if (backImage is File) {
          request.files.add(
            await http.MultipartFile.fromPath('backImage', backImage.path),
          );
        } else if (backImage is String) {
          request.fields['backImageUrl'] = backImage; // URL cũ
        }
      }
      if (faceImage != null) {
        if (faceImage is File) {
          request.files.add(
            await http.MultipartFile.fromPath('faceImage', faceImage.path),
          );
        } else if (faceImage is String) {
          request.fields['faceImageUrl'] = frontImage; // URL cũ
        }
      }

      final response = await request.send();
      final respStr = await response.stream.bytesToString();
      Map<String, dynamic> parsedBody = {};
      try {
        parsedBody = jsonDecode(respStr);
      } catch (_) {
        // Nếu không decode được thì để parsedBody rỗng
      }
      return {
        "status": response.statusCode,
        "success": response.statusCode == 200,
        "body": respStr,
        "errors": parsedBody["errors"] is Map ? parsedBody["errors"] : {},
      };
    } catch (e) {
      throw Exception('Connection error: $e');
    }
  }

  ///Documents
  //Get Personal Data
  static Future<List<dynamic>> getCustomerDocuments(int customerId) async {
    final url = Uri.parse('$customerUrl/documents/$customerId');
    try {
      final response = await http.get(url, headers: headers);
      if (response.statusCode == 200) {
        final decoded = jsonDecode(utf8.decode(response.bodyBytes));
        print(decoded);
        if (decoded is List) {
          return decoded;
        } else {
          throw Exception('Expected List but got ${decoded.runtimeType}');
        }
      } else {
        throw Exception('Server error: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Connection error: $e');
    }
  }

  //Create Document
  static Future<Map<String, dynamic>> createDocument({
    required int customerId,
    required Map<String, dynamic> documentDto,
    required File? frontImage,
    required File? backImage,
  }) async {
    String? token = loginResponse?.token;
    final uri = Uri.parse('$customerUrl/documents/createNew');
    try {
      final documentDtoString = jsonEncode(documentDto);
      final request =
          http.MultipartRequest('POST', uri)
            ..fields['documentString'] = documentDtoString
            ..fields['userIdString'] = customerId.toString()
            ..headers.addAll({
              'Content-Type': 'multipart/form-data',
              'Authorization': 'Bearer $token',
            });

      if (frontImage != null) {
        request.files.add(
          await http.MultipartFile.fromPath('frontImage', frontImage.path),
        );
      }
      if (backImage != null) {
        request.files.add(
          await http.MultipartFile.fromPath('backImage', backImage.path),
        );
      }
      final response = await request.send();
      final respStr = await response.stream.bytesToString();
      Map<String, dynamic> parsedBody = {};
      try {
        parsedBody = jsonDecode(respStr);
      } catch (_) {
        // Nếu không decode được thì để parsedBody rỗng
      }
      return {
        "status": response.statusCode,
        "success": response.statusCode == 200,
        "body": respStr,
        "errors": parsedBody["errors"] is Map ? parsedBody["errors"] : {},
      };
    } catch (e) {
      throw Exception('Connection error: $e');
    }
  }

  //Update Document
  static Future<Map<String, dynamic>> updateDocument({
    required int documentId,
    required Map<String, dynamic> documentDto,
    required dynamic frontImage,
    required dynamic backImage,
  }) async {
    String? token = loginResponse?.token;
    final uri = Uri.parse(
      '$customerUrl/documents/updateDocument/${documentId}',
    );
    try {
      final documentDtoString = jsonEncode(documentDto);
      final request =
          http.MultipartRequest('PUT', uri)
            ..fields['sDocumentId'] = documentId.toString()
            ..fields['documentDtoString'] = documentDtoString
            ..headers.addAll({
              'Accept': 'application/json',
              'Authorization': 'Bearer $token',
            });

      if (frontImage != null) {
        if (frontImage is File) {
          request.files.add(
            await http.MultipartFile.fromPath('frontImage', frontImage.path),
          );
        } else if (frontImage is String) {
          request.fields['frontImageUrl'] = frontImage; // URL cũ
        }
      }
      if (backImage != null) {
        if (backImage is File) {
          request.files.add(
            await http.MultipartFile.fromPath('backImage', backImage.path),
          );
        } else if (backImage is String) {
          request.fields['backImageUrl'] = backImage;
        }
      }

      final response = await request.send();
      final respStr = await response.stream.bytesToString();
      Map<String, dynamic> parsedBody = {};
      try {
        parsedBody = jsonDecode(respStr);
      } catch (_) {
        // Nếu không decode được thì để parsedBody rỗng
      }
      return {
        "status": response.statusCode,
        "success": response.statusCode == 200,
        "body": respStr,
        "errors": parsedBody["errors"] is Map ? parsedBody["errors"] : {},
      };
    } catch (e) {
      throw Exception('Connection error: $e');
    }
  }

  //Delete Document
  static Future<void> deleteDocument(int documentId) async {
    final url = Uri.parse('$customerUrl/documents/$documentId');
    try {
      final response = await http.delete(url, headers: headers);

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        print("Delete sucess: ${data['message']}");
      } else if (response.statusCode == 204) {
        print("Delete sucess (no content)");
      } else {
        print("❌ Delete Fail: ${response.statusCode}");
        print("Fail: ${response.body}");
      }
    } catch (e) {
      print("⚠️ Call API error: $e");
    }
  }

  ///Discount
  //Get App Discount
  static Future<List<dynamic>> getAppDiscount(int customerId) async {
    final url = Uri.parse('$customerUrl/discounts/appDiscounts/$customerId');
    try {
      final response = await http.get(url, headers: headers);
      if (response.statusCode == 200) {
        final decoded = jsonDecode(utf8.decode(response.bodyBytes));
        if (decoded is List) {
          return decoded;
        } else {
          throw Exception('Expected List but got ${decoded.runtimeType}');
        }
      } else {
        throw Exception('Server error: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Connection error: $e');
    }
  }

  //Get Rank Discount
  static Future<List<dynamic>> getRankDiscount(int customerId) async {
    final url = Uri.parse('$customerUrl/discounts/rankDiscounts/$customerId');
    try {
      final response = await http.get(url, headers: headers);
      if (response.statusCode == 200) {
        final decoded = jsonDecode(utf8.decode(response.bodyBytes));
        if (decoded is List) {
          return decoded;
        } else {
          throw Exception('Expected List but got ${decoded.runtimeType}');
        }
      } else {
        throw Exception('Server error: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Connection error: $e');
    }
  }

  //Get My Discount
  static Future<List<dynamic>> getMyDiscount(int customerId) async {
    final url = Uri.parse('$customerUrl/discounts/myDiscounts/$customerId');
    try {
      final response = await http.get(url, headers: headers);
      if (response.statusCode == 200) {
        final decoded = jsonDecode(utf8.decode(response.bodyBytes));
        if (decoded is List) {
          return decoded;
        } else {
          throw Exception('Expected List but got ${decoded.runtimeType}');
        }
      } else {
        throw Exception('Server error: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Connection error: $e');
    }
  }

  //Claim Discount

  static Future<String> claimDiscount(int discountId, int userId) async {
    final url = Uri.parse('$customerUrl/discounts/claimDiscount');
    try {
      final response = await http.post(
        url,
        headers: headers,
        body: jsonEncode({'discountId': discountId, 'userId': userId}),
      );

      if (response.statusCode == 200) {
        return 'success';
      } else if (response.statusCode == 404) {
        return 'no-change-left';
      } else {
        return 'error';
      }
    } catch (e) {
      print('Exception while claiming discount: $e');
      return 'error';
    }
  }

  ///Payment
  // //User Pay
  // static Future<String?> createUserPayment(int rrId) async {
  //   final url = Uri.parse('$paymentUrl/pay-rescue?rrId=$rrId');

  //   try {
  //     final response = await http.post(url);

  //     if (response.statusCode == 200) {
  //       final data = jsonDecode(response.body);
  //       return data['approveUrl']; // link để redirect qua PayPal
  //     } else {
  //       final error = jsonDecode(response.body)['error'];
  //       throw Exception(error ?? 'Cannot create payment.');
  //     }
  //   } catch (e) {
  //     print("Lỗi khi gọi PayPal: $e");
  //     return null;
  //   }
  // }

  //Get Payments
  static Future<List<dynamic>> getPayments(int customerId) async {
    final url = Uri.parse('$customerUrl/payments/$customerId');
    try {
      final response = await http.get(url, headers: headers);
      if (response.statusCode == 200) {
        final decoded = jsonDecode(utf8.decode(response.bodyBytes));
        print(decoded);
        if (decoded is List) {
          return decoded;
        } else {
          throw Exception('Expected List but got ${decoded.runtimeType}');
        }
      } else {
        throw Exception('Server error: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Connection error: $e');
    }
  }

  //Create Document
  static Future<Map<String, dynamic>> createPayment({
    required int customerId,
    required Map<String, dynamic> paymentDto,
  }) async {
    final uri = Uri.parse('$customerUrl/payments/createNew/$customerId');
    try {
      final paymentDtoString = jsonEncode(paymentDto);
      print(paymentDtoString);
      final request =
          http.MultipartRequest('POST', uri)
            ..fields['paymentDtoString'] = paymentDtoString
            ..headers.addAll({
              'Accept': 'application/json',
              'Authorization': 'Bearer $token',
            });

      final response = await request.send();
      final respStr = await response.stream.bytesToString();
      Map<String, dynamic> parsedBody = {};
      try {
        parsedBody = jsonDecode(respStr);
      } catch (_) {
        // Nếu không decode được thì để parsedBody rỗng
      }
      return {
        "status": response.statusCode,
        "success": response.statusCode == 200,
        "body": respStr,
        "errors": parsedBody["errors"] is Map ? parsedBody["errors"] : {},
      };
    } catch (e) {
      throw Exception('Connection error: $e');
    }
  }

  //Update Payment
  static Future<Map<String, dynamic>> updatePayment({
    required int paymentId,
    required Map<String, dynamic> paymentDto,
  }) async {
    final uri = Uri.parse('$customerUrl/payments/updatePayment/${paymentId}');
    try {
      final paymentDtoString = jsonEncode(paymentDto);
      final request =
          http.MultipartRequest('PUT', uri)
            ..fields['paymentDtoString'] = paymentDtoString
            ..headers.addAll({
              'Accept': 'application/json',
              'Authorization': 'Bearer $token',
            });

      final response = await request.send();
      final respStr = await response.stream.bytesToString();
      Map<String, dynamic> parsedBody = {};
      try {
        parsedBody = jsonDecode(respStr);
      } catch (_) {
        // Nếu không decode được thì để parsedBody rỗng
      }
      return {
        "status": response.statusCode,
        "success": response.statusCode == 200,
        "body": respStr,
        "errors": parsedBody["errors"] is Map ? parsedBody["errors"] : {},
      };
    } catch (e) {
      throw Exception('Connection error: $e');
    }
  }

  //Delete Payment
  static Future<void> deletePayment(int paymentId) async {
    final url = Uri.parse('$customerUrl/payments/$paymentId');
    print(url);
    print(paymentId);
    try {
      final response = await http.delete(url, headers: headers);

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        print("Delete sucess: ${data['message']}");
      } else if (response.statusCode == 204) {
        print("Delete sucess (no content)");
      } else {
        print("❌ Delete Fail: ${response.statusCode}");
        print("Fail: ${response.body}");
      }
    } catch (e) {
      print("⚠️ Call API error: $e");
    }
  }
}
