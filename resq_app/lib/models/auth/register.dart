class Register {
  final String fullName;
  final String password;
  final String email;
  final String phoneNumber;
  final String gender;
  final String dob;

  Register({
    required this.fullName,
    required this.password,
    required this.email,
    required this.phoneNumber,
    required this.gender,
    required this.dob,
  });

  Map<String, dynamic> toJson() {
    return {
      'fullName': fullName,
      'password': password,
      'email': email,
      'sdt': phoneNumber,
      'gender': gender,
      'dob': dob,
    };
  }
}
