class Bill {
  final int billId;
  final double servicePrice;
  final double distancePrice;
  final double extraPrice;
  final double totalPrice;
  final double appFee;
  final double discountAmount;
  final double total;
  final String method;
  final String status;
  final String currency;

  Bill({
    required this.billId,
    required this.servicePrice,
    required this.distancePrice,
    required this.extraPrice,
    required this.totalPrice,
    required this.appFee,
    required this.discountAmount,
    required this.total,
    required this.method,
    required this.status,
    required this.currency,
  });

  factory Bill.fromJson(Map<String, dynamic> json) {
    return Bill(
      billId: json['billId'],
      servicePrice: json['servicePrice']?.toDouble() ?? 0.0,
      distancePrice: json['distancePrice']?.toDouble() ?? 0.0,
      extraPrice: json['extraPrice']?.toDouble() ?? 0.0,
      totalPrice: json['totalPrice']?.toDouble() ?? 0.0,
      appFee: json['appFee']?.toDouble() ?? 0.0,
      discountAmount: json['discountAmount']?.toDouble() ?? 0.0,
      total: json['total']?.toDouble() ?? 0.0,
      method: json['method'],
      status: json['status'],
      currency: json['currency'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'billId': billId,
      'servicePrice': servicePrice,
      'distancePrice': distancePrice,
      'extraPrice': extraPrice,
      'totalPrice': totalPrice,
      'appFee': appFee,
      'discountAmount': discountAmount,
      'total': total,
      'method': method,
      'status': status,
      'currency': currency,
    };
  }
}
