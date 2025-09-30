class TrackingData {
  final int rrid;
  final double userLatitude;
  final double userLongitude;
  final double partnerLatitude;
  final double partnerLongitude;
  final bool realtimeUpdate;
  final String status;

  TrackingData({
    required this.rrid,
    required this.userLatitude,
    required this.userLongitude,
    required this.partnerLatitude,
    required this.partnerLongitude,
    required this.realtimeUpdate,
    required this.status,
  });

  factory TrackingData.fromJson(Map<String, dynamic> json) {
    return TrackingData(
      rrid: json['rrid'],
      userLatitude: json['userLatitude']?.toDouble() ?? 0.0,
      userLongitude: json['userLongitude']?.toDouble() ?? 0.0,
      partnerLatitude: json['partnerLatitude']?.toDouble() ?? 0.0,
      partnerLongitude: json['partnerLongitude']?.toDouble() ?? 0.0,
      realtimeUpdate: json['realtimeUpdate'] ?? false,
      status: json['status'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'rrid': rrid,
      'userLatitude': userLatitude,
      'userLongitude': userLongitude,
      'partnerLatitude': partnerLatitude,
      'partnerLongitude': partnerLongitude,
      'realtimeUpdate': realtimeUpdate,
      'status': status,
    };
  }
}
