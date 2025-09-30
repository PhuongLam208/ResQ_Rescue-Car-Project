class PDImageDto {
  final String frontImageUrl;
  final String backImageUrl;
  final String verificationStatus;

  PDImageDto({
    required this.frontImageUrl,
    required this.backImageUrl,
    required this.verificationStatus,
  });

  factory PDImageDto.fromJson(Map<String, dynamic> json) {
    return PDImageDto(
      frontImageUrl: json['frontImageUrl'] ?? '',
      backImageUrl: json['backImageUrl'] ?? '',
      verificationStatus: json['verificationStatus'] ?? '',
    );
  }
}
