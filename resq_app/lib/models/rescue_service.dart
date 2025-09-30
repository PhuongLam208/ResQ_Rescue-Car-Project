class RescueService {
  final int serviceId;
  final String serviceName;
  final String serviceType;
  final double fixedPrice;
  final double pricePerKm;
  final int quantity;
  final double price;

  RescueService({
    required this.serviceId,
    required this.serviceName,
    required this.serviceType,
    required this.fixedPrice,
    required this.pricePerKm,
    this.quantity = 1,
    this.price = 0.0,
  });

  factory RescueService.fromJson(Map<String, dynamic> json) {
    return RescueService(
      serviceId: json['serviceId'],
      serviceName: json['serviceName'],
      serviceType: json['serviceType'],
      fixedPrice: json['fixedPrice']?.toDouble() ?? 0.0,
      pricePerKm: json['pricePerKm']?.toDouble() ?? 0.0,
      quantity: json['quantity'] ?? 1,
      price: json['price']?.toDouble() ?? 0.0,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'serviceId': serviceId,
      'serviceName': serviceName,
      'serviceType': serviceType,
      'fixedPrice': fixedPrice,
      'pricePerKm': pricePerKm,
      'quantity': quantity,
      'price': price,
    };
  }
}
