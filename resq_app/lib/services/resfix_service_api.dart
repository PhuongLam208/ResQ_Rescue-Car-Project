import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:resq_app/models/auth/login_response.dart';
import '../config/app_config.dart'; // Đường dẫn tới file chứa baseUrl, headers

class ResfixServiceApi {
  static const String serviceEndpoint = '/api/resq/pcrescue/service';

  static Future<List<Map<String, dynamic>>> fetchResfixDamages() async {
    final uri = Uri.parse(
      '$baseUrl$serviceEndpoint',
    ).replace(queryParameters: {'type': 'ResFix'});

    final response = await http.get(uri, headers: headers);

    if (response.statusCode == 200) {
      final jsonBody = json.decode(response.body);
      final List<dynamic> data = jsonBody['data'];

      // Map từng item sang dạng Map<String, dynamic>
      return data.map<Map<String, dynamic>>((item) {
        return {'name': item['srvName'], 'price': item['srvPrice'] ?? 0};
      }).toList();
    } else {
      throw Exception('Không thể tải danh sách dịch vụ ResFix');
    }
  }

  
}
