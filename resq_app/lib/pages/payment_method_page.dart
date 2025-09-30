// lib/pages/payment_method_page.dart
import 'package:flutter/material.dart';

class PaymentMethodPage extends StatefulWidget {
  final String? currentSelectedMethod;

  const PaymentMethodPage({Key? key, this.currentSelectedMethod})
    : super(key: key);

  @override
  State<PaymentMethodPage> createState() => _PaymentMethodPageState();
}

class _PaymentMethodPageState extends State<PaymentMethodPage> {
  static const primaryColor = Color(0xFF062D4E);

  String? _selectedMethod;
  List<String> _availablePaymentMethods = ['CASH', 'Paypal'];
  // Danh sách mock

  @override
  void initState() {
    super.initState();
    _selectedMethod = widget.currentSelectedMethod;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          "Select Payment Method",
          style: TextStyle(color: Colors.white, fontWeight: FontWeight.w600),
        ),
        backgroundColor: primaryColor,
        iconTheme: const IconThemeData(color: Colors.white),
        elevation: 0,
      ),
      body: Column(
        children: [
          Expanded(
            child: ListView.builder(
              padding: const EdgeInsets.all(16.0),
              itemCount: _availablePaymentMethods.length,
              itemBuilder: (context, index) {
                final method = _availablePaymentMethods[index];
                return Card(
                  margin: const EdgeInsets.symmetric(vertical: 8.0),
                  elevation: 2,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: RadioListTile<String>(
                    title: Text(
                      method,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w500,
                        color: Colors.black87,
                      ),
                    ),
                    value: method,
                    groupValue: _selectedMethod,
                    onChanged: (String? value) {
                      setState(() {
                        _selectedMethod = value;
                      });
                    },
                    secondary: Icon(
                      _getPaymentIcon(method),
                      color: primaryColor,
                    ), // Thêm icon
                    controlAffinity:
                        ListTileControlAffinity.trailing, // Radio button ở cuối
                    activeColor: primaryColor,
                    tileColor:
                        _selectedMethod == method
                            ? primaryColor.withOpacity(0.05)
                            : null,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                  ),
                );
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: SizedBox(
              width: double.infinity,
              height: 54,
              child: ElevatedButton(
                onPressed:
                    _selectedMethod == null
                        ? null // Disable button if no method is selected
                        : () {
                          // Trả về phương thức thanh toán đã chọn cho trang trước
                          Navigator.pop(context, _selectedMethod);
                        },
                style: ElevatedButton.styleFrom(
                  backgroundColor: primaryColor,
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(16),
                  ),
                  elevation: 4,
                ),
                child: const Text(
                  "Confirm Selection",
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  // Hàm helper để lấy icon tương ứng với phương thức thanh toán
  IconData _getPaymentIcon(String method) {
    switch (method) {
      case 'CASH':
        return Icons.money;
      case 'Paypal':
        return Icons
            .account_balance_wallet; // hoặc đổi sang Icons.payments nếu thích
      default:
        return Icons.payment;
    }
  }
}
