import 'package:flutter/material.dart';

class SelectPaymentPage extends StatelessWidget {
  const SelectPaymentPage({super.key});

  @override
  Widget build(BuildContext context) {
    final payments = ["Tiền mặt", "Thẻ ngân hàng", "Momo"];

    return Scaffold(
      appBar: AppBar(title: const Text("Phương thức thanh toán")),
      body: ListView(
        children:
            payments
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
