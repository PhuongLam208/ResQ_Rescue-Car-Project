class StaffInfo {
  final int staffId;
  final String staffName;

  StaffInfo({required this.staffId, required this.staffName});

  factory StaffInfo.fromJson(Map<String, dynamic> json) {
    return StaffInfo(
      staffId: json['staffId'],
      staffName: json['staffName'],
    );
  }
}
