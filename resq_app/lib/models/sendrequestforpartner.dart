class SendReqquestToPartner {
  final int rrId;
  final String? address; // Maps to locationAddress
  final String? rescueType; // Maps to serviceType
  final double? latitude; // Maps to lat
  final double? longitude; // Maps to lon
  final DateTime? createdAt; // Maps to timestamp

  SendReqquestToPartner({
    required this.rrId,
    this.address,
    this.rescueType,
    this.latitude,
    this.longitude,
    this.createdAt,
  });

  factory SendReqquestToPartner.fromJson(Map<String, dynamic> json) {
    return SendReqquestToPartner(
      rrId: json['id'] as int,
      address: json['locationAddress'] as String?, // Key changed
      rescueType: json['serviceType'] as String?, // Key changed
      latitude: (json['lat'] as num?)?.toDouble(), // Key changed
      longitude: (json['lon'] as num?)?.toDouble(), // Key changed
      createdAt:
          json['timestamp'] !=
                  null // Key changed
              ? DateTime.parse(json['timestamp'] as String)
              : null,
    );
  }
}
