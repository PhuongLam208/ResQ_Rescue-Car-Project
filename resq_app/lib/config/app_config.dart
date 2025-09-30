import '../models/auth/login_response.dart';

const String baseUrl = 'http://192.168.185.229:9090';
String wsBaseUrl = 'http://192.168.185.229:9090';
// String wsBaseUrl = 'http://10.0.2.2:9090';
final String mapboxToken =
    "pk.eyJ1IjoidHJhbXRyYW4xMjMiLCJhIjoiY21kNGRkMHQ0MGY2NTJscjZmcDY4bzVuNCJ9.L4-zGwpDVXx9aKqTqbDyvA";

//  Lấy token từ biến toàn cục
String get token => loginResponse?.token ?? '';

Map<String, String> get headers => {
  'Content-Type': 'application/json',
  'Authorization': 'Bearer $token',
};

const String verifyUrl = '$baseUrl/api/resq/verify';
const String customerUrl = '$baseUrl/api/resq/customer';
const String partnerUrl = "$baseUrl/api/resq/partner";
const String avatarUrl = "$baseUrl/uploads/avatar";
const String paymentUrl = "$baseUrl/api/paypal";
const String partner_chatUrl = "$baseUrl/api/resq/partner-chat";
const String resqUrl = "$baseUrl/api/resq";
