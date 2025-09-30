import 'package:resq_app/models/vehicle.dart';

class Partner {
  final int partnerId;
  final String name;
  final String phone;
  final String location;
  final double avgTime;
  final bool onWorking;
  final String status;
  final List<Vehicle>? vehicles;

  Partner({
    required this.partnerId,
    required this.name,
    required this.phone,
    required this.location,
    required this.avgTime,
    required this.onWorking,
    required this.status,
    this.vehicles,
  });

  factory Partner.fromJson(Map<String, dynamic> json) {
    return Partner(
      partnerId: json['partnerId'],
      name: json['name'],
      phone: json['phone'],
      location: json['location'],
      avgTime: json['avgTime']?.toDouble() ?? 0.0,
      onWorking: json['onWorking'] ?? false,
      status: json['status'],
      vehicles:
          json['vehicles'] != null
              ? (json['vehicles'] as List)
                  .map((vehicle) => Vehicle.fromJson(vehicle))
                  .toList()
              : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'partnerId': partnerId,
      'name': name,
      'phone': phone,
      'location': location,
      'avgTime': avgTime,
      'onWorking': onWorking,
      'status': status,
      'vehicles': vehicles?.map((vehicle) => vehicle.toJson()).toList(),
    };
  }
}
