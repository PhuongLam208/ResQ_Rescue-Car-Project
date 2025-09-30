class LoginResponse {
  final int userId;
  final String token;
  final String userName;
  final String role;
  final bool isOnline;

  LoginResponse({
    required this.userId,
    required this.token,
    required this.userName,
    required this.role,
    required this.isOnline,
  });

  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    return LoginResponse(
      userId: json['userId'],
      token: json['token'],
      userName: json['fullName'],
      role: json['role'],
      isOnline: json['onShift'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'userId': userId,
      'token': token,
      'fullName': userName,
      'role': role,
      'onShift': isOnline,
    };
  }
}

LoginResponse? loginResponse;
