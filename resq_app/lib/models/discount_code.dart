class DiscountCode {
  final String code;
  final double amount;
  final String name;
  final String type; // "Money" hoặc "Percent"
  final String description;

  DiscountCode({
    required this.code,
    required this.amount,
    required this.name,
    required this.type,
    required this.description,
  });

  factory DiscountCode.fromJson(Map<String, dynamic> json) {
    return DiscountCode(
      code: json['code'],
      amount: json['amount'].toDouble(),
      name: json['name'],
      type: json['type'],
      description: json['description'] ?? '',
    );
  }
}
