class ServiceRequest {
  final int serviceId;
  final int quantity;
  final double customPrice;

  ServiceRequest({
    required this.serviceId,
    required this.quantity,
    this.customPrice = 0.0,
  });

  factory ServiceRequest.fromJson(Map<String, dynamic> json) {
    return ServiceRequest(
      serviceId: json['serviceId'],
      quantity: json['quantity'],
      customPrice: json['customPrice']?.toDouble() ?? 0.0,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'serviceId': serviceId,
      'quantity': quantity,
      'customPrice': customPrice,
    };
  }
}
