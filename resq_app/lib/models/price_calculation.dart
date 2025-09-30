import 'package:resq_app/models/service_request.dart';

class PriceCalculation {
  final String rescueType;
  final List<ServiceRequest> services;
  final double distance;
  final String? discountCode;
  final double servicePrice;
  final double distancePrice;
  final double extraPrice;
  final double totalPrice;
  final double appFee;
  final double discountAmount;
  final double finalTotal;
  final String currency;

  PriceCalculation({
    required this.rescueType,
    required this.services,
    required this.distance,
    this.discountCode,
    this.servicePrice = 0.0,
    this.distancePrice = 0.0,
    this.extraPrice = 0.0,
    this.totalPrice = 0.0,
    this.appFee = 0.0,
    this.discountAmount = 0.0,
    this.finalTotal = 0.0,
    this.currency = 'VND',
  });

  factory PriceCalculation.fromJson(Map<String, dynamic> json) {
    return PriceCalculation(
      rescueType: json['rescueType'],
      services:
          (json['services'] as List)
              .map((service) => ServiceRequest.fromJson(service))
              .toList(),
      distance: json['distance']?.toDouble() ?? 0.0,
      discountCode: json['discountCode'],
      servicePrice: json['servicePrice']?.toDouble() ?? 0.0,
      distancePrice: json['distancePrice']?.toDouble() ?? 0.0,
      extraPrice: json['extraPrice']?.toDouble() ?? 0.0,
      totalPrice: json['totalPrice']?.toDouble() ?? 0.0,
      appFee: json['appFee']?.toDouble() ?? 0.0,
      discountAmount: json['discountAmount']?.toDouble() ?? 0.0,
      finalTotal: json['finalTotal']?.toDouble() ?? 0.0,
      currency: json['currency'] ?? 'VND',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'rescueType': rescueType,
      'services': services.map((service) => service.toJson()).toList(),
      'distance': distance,
      'discountCode': discountCode,
      'servicePrice': servicePrice,
      'distancePrice': distancePrice,
      'extraPrice': extraPrice,
      'totalPrice': totalPrice,
      'appFee': appFee,
      'discountAmount': discountAmount,
      'finalTotal': finalTotal,
      'currency': currency,
    };
  }
}
