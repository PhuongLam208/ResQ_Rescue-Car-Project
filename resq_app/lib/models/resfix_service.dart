class ResFixService {
  final int id;
  final String name;
  final double fixedPrice;
  final String type;
  final double pricePerKm;

  ResFixService({
    required this.id,
    required this.name,
    required this.fixedPrice,
    required this.type,
    required this.pricePerKm,
  });

  factory ResFixService.fromJson(Map<String, dynamic> json) {
    return ResFixService(
      id: json['id'],
      name: json['name'],
      fixedPrice: (json['fixedPrice'] as num).toDouble(),
      type: json['type'],
      pricePerKm: (json['pricePerKm'] as num).toDouble(),
    );
  }
}
