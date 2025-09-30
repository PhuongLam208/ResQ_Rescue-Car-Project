class LoginRequest {
  final String phoneNumber;
  final String password;

  LoginRequest({required this.phoneNumber, required this.password});

  Map<String, dynamic> toJson() {
    return {
      'loginName': phoneNumber,
      'password': password,
    };
  }
}
