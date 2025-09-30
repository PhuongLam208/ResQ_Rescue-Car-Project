import 'dart:convert';
import 'package:http/http.dart' as http;

class GeocodingService {
  final String apiKey =
      "pk.fe5593e84f4321f047419f6504a6f2a5"; // Thay bằng key của bạn

  // Tìm kiếm địa chỉ theo text
  Future<List<dynamic>> search(String query) async {
    final url = Uri.parse(
      "https://us1.locationiq.com/v1/autocomplete?key=$apiKey&q=$query&format=json",
    );
    final response = await http.get(url);
    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception('Failed to load location');
    }
  }

  // Lấy địa chỉ (display name) từ tọa độ (lat, lon)
  Future<String?> reverse(double lat, double lon) async {
    final url = Uri.parse(
      "https://us1.locationiq.com/v1/reverse?key=$apiKey&lat=$lat&lon=$lon&format=json",
    );
    final response = await http.get(url);
    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return data['display_name'];
    } else {
      print('Reverse geocoding failed: ${response.body}');
      return null;
    }
  }
}
// import 'dart:convert';
// import 'package:http/http.dart' as http;

// class GeocodingService {
//   final String mapboxToken = "pk...."; // Thay bằng token của bạn

//   // Tìm kiếm
//   Future<List<dynamic>> search(String query) async {
//     final url = Uri.parse(
//         "https://api.mapbox.com/geocoding/v5/mapbox.places/$query.json?access_token=$mapboxToken&autocomplete=true&limit=5");
//     final response = await http.get(url);
//     if (response.statusCode == 200) {
//       final data = jsonDecode(response.body);
//       return data['features'];
//     } else {
//       throw Exception('Failed to load location');
//     }
//   }

//   // Lấy địa chỉ từ tọa độ
//   Future<String?> reverse(double lat, double lon) async {
//     final url = Uri.parse(
//         "https://api.mapbox.com/geocoding/v5/mapbox.places/$lon,$lat.json?access_token=$mapboxToken&limit=1");
//     final response = await http.get(url);
//     if (response.statusCode == 200) {
//       final data = jsonDecode(response.body);
//       if (data['features'] != null && data['features'].isNotEmpty) {
//         return data['features'][0]['place_name'];
//       }
//     }
//     print('Reverse geocoding failed: ${response.body}');
//     return null;
//   }
// }
