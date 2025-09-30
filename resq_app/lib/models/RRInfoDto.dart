class RRInfoDto {
  final int rrId;
  final String description;
  final String paymentMethod;
  final double total;

  RRInfoDto({
    required this.rrId,
    required this.description,
    required this.paymentMethod,
    required this.total,
  });

  factory RRInfoDto.fromJson(Map<String, dynamic> json) {
    return RRInfoDto(
      rrId: json['rrId'],
      description: json['description'],
      paymentMethod: json['paymentMethod'],
      total: (json['total'] as num).toDouble(),
    );
  }
}
