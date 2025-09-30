class Breakdown {
  final String location;
  final String destination;
  final String serviceName;
  final String estimatedTime;
  final String status;
  final String driverName;
  final String vehicle;

  Breakdown({
    required this.location,
    required this.destination,
    required this.serviceName,
    required this.estimatedTime,
    required this.status,
    required this.driverName,
    required this.vehicle,
  });

  factory Breakdown.fromJson(Map<String, dynamic> json) {
    return Breakdown(
      location: json['location'] ?? '',
      destination: json['destination'] ?? '',
      serviceName: json['serviceName'] ?? '',
      estimatedTime: json['estimatedTime'] ?? '',
      status: json['status'] ?? '',
      driverName: json['driverName'] ?? '',
      vehicle: json['vehicle'] ?? '',
    );
  }
}
