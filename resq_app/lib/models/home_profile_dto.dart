import '../config/app_config.dart';

class HomeProfileDto {
  final String status;
  final bool hasPD;
  final String userName;
  final String avatar;
  final int loyaltyPoint;

  HomeProfileDto({
    required this.status,
    required this.hasPD,
    required this.userName,
    required this.avatar,
    required this.loyaltyPoint,
  });

  factory HomeProfileDto.fromJson(Map<String, dynamic> json) {
    return HomeProfileDto(
      status: json['status'] ?? '',
      hasPD: json['hasPD'] ?? false,
      userName: json['userName'] ?? '',
      avatar: json['avatar'] ?? '',
      loyaltyPoint: json['loyaltyPoint'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'status': status,
      'hasPD': hasPD,
      'userName': userName,
      'avatar': avatar,
      'loyaltyPoint': loyaltyPoint,
    };
  }

  String get imageUrl {
    if (avatar == null || avatar!.isEmpty) {
      return '/assets/images/Logo.png';
    }
    return '$baseUrl/$avatar';
  }
}
