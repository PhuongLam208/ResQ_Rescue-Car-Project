import 'package:flutter/material.dart';

class SelectPromoPage extends StatelessWidget {
  const SelectPromoPage({super.key});

  @override
  Widget build(BuildContext context) {
    final promos = ["Giảm 15% tối đa 30k", "Giảm 10% tối đa 20k"];

    return Scaffold(
      appBar: AppBar(title: const Text("Ưu đãi")),
      body: ListView(
        children:
            promos
                .map(
                  (p) => ListTile(
                    title: Text(p),
                    onTap: () => Navigator.pop(context, p),
                  ),
                )
                .toList(),
      ),
    );
  }
}
