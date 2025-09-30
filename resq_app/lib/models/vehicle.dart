class Vehicle {
  final int vehicleId;
  final String vehicleType;
  final String licensePlate;
  final String brand;
  final String model;
  final int year;

  Vehicle({
    required this.vehicleId,
    required this.vehicleType,
    required this.licensePlate,
    required this.brand,
    required this.model,
    required this.year,
  });

  factory Vehicle.fromJson(Map<String, dynamic> json) {
    return Vehicle(
      vehicleId: json['vehicleId'],
      vehicleType: json['vehicleType'],
      licensePlate: json['licensePlate'],
      brand: json['brand'],
      model: json['model'],
      year: json['year'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'vehicleId': vehicleId,
      'vehicleType': vehicleType,
      'licensePlate': licensePlate,
      'brand': brand,
      'model': model,
      'year': year,
    };
  }
}
