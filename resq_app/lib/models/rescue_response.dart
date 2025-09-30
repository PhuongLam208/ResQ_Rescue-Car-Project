import 'package:resq_app/models/breakdown.dart';

class RescueResponse {
  final int rrid;
  final int billId;
  final double total;
  final String currency;
  final String method;
  final Breakdown breakdown;

  RescueResponse({
    required this.rrid,
    required this.billId,
    required this.total,
    required this.currency,
    required this.method,
    required this.breakdown,
  });

  factory RescueResponse.fromJson(Map<String, dynamic> json) {
    // final data = json['data'];
    return RescueResponse(
      rrid: json['rrid'] ?? 0,
      billId: json['billId'] ?? 0,
      total: (json['total'] ?? 0).toDouble(),
      currency: json['currency'] ?? '',
      method: json['method'] ?? '',
      breakdown: Breakdown.fromJson(json['breakdown'] ?? {}),
    );
  }
}
