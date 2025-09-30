class RescueRequestNotificationDto {
  final int rrid;
  final int? partnerId;

  final double startLatitude;
  final double startLongitude;
  final double endLatitude;
  final double endLongitude;

  final String message;
  final String userFullName;
  final String from;
  final String to;
  final String serviceType;
  final String startAddress;
  final String endAddress;

  final double discountAmount;
  final double finalPrice;
  final String paymentMethod;

  RescueRequestNotificationDto({
    required this.rrid,
    this.partnerId,
    required this.startLatitude,
    required this.startLongitude,
    required this.endLatitude,
    required this.endLongitude,
    required this.message,
    required this.userFullName,
    required this.from,
    required this.to,
    required this.serviceType,
    required this.startAddress,
    required this.endAddress,
    required this.discountAmount,
    required this.finalPrice,
    required this.paymentMethod,
  });

  factory RescueRequestNotificationDto.fromJson(Map<String, dynamic> json) {
    return RescueRequestNotificationDto(
      rrid: json['rrid'],
      partnerId: json['partnerId'], // nullable OK nếu là int?
      startLatitude: json['startLatitude'] ?? 0.0,
      startLongitude: json['startLongitude'] ?? 0.0,
      endLatitude: json['endLatitude'] ?? 0.0,
      endLongitude: json['endLongitude'] ?? 0.0,
      message: json['message'] ?? '',
      userFullName: json['userFullName'] ?? '',
      from: json['from'] ?? '',
      to: json['to'] ?? '',
      serviceType: json['serviceType'] ?? '',
      startAddress: json['startAddress'] ?? '',
      endAddress: json['endAddress'] ?? '',
      discountAmount: (json['discountAmount'] ?? 0).toDouble(),
      finalPrice: (json['finalPrice'] ?? 0).toDouble(),
      paymentMethod: json['paymentMethod'] ?? '',
    );
  }
}
