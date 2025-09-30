import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:resq_app/services/customer_service.dart';
import 'package:resq_app/widgets/common_app_bar.dart';

class PaymentDetailPage extends StatefulWidget {
  final Map<String, dynamic> payment;
  const PaymentDetailPage({super.key, required this.payment});

  @override
  State<PaymentDetailPage> createState() => _PaymentDetailPageState();
}

class _PaymentDetailPageState extends State<PaymentDetailPage> {
  final TextEditingController methodController = TextEditingController();
  final TextEditingController nameController = TextEditingController();
  final TextEditingController paypalEmailController = TextEditingController();
  Map<String, String> _errors = {};

  @override
  void initState() {
    super.initState();
    _loadInitialData();
  }

  void _loadInitialData() {
    final pay = widget.payment;
    methodController.text = pay['method'] ?? '';
    nameController.text = pay['name'] ?? '';
    paypalEmailController.text = pay['paypalEmail'] ?? '';
  }

  Future<void> _handleUpdate() async {
    final method = methodController.text.trim();
    final name = nameController.text.trim();
    final paypalEmail = paypalEmailController.text.trim();

    final dto = {
      "method": method,
      "name": name,
      "paypalEmail": paypalEmail,
    };

    final result = await CustomerService.updatePayment(
      paymentId: widget.payment['paymentId'],
      paymentDto: dto,
    );

    final bool success = result["success"] == true;
    if (success) {
      setState(() {
        _errors.clear();
      });
      _showDialog("Update Success", "Payment updated successfully!");
    } else {
      final dynamic errors = result["errors"];
      setState(() {
        _errors = (errors is Map)
            ? errors.map((k, v) => MapEntry(k.toString(), v.toString()))
            : {};
      });
    }
  }

  void _showDialog(String title, String message) {
    showDialog(
      context: context,
      builder: (_) => AlertDialog(
        title: Center(
          child: Text(title, style: TextStyle(color: Colors.green[900], fontWeight: FontWeight.bold, fontSize: 23)),
        ),
        content: Text(message),
      ),
    );

    Future.delayed(const Duration(seconds: 2), () {
      if (mounted) {
        Navigator.of(context, rootNavigator: true).pop();
        Navigator.pop(context, true);
      }
    });
  }

  Widget _buildInput(
      TextEditingController controller, {
        required String fieldName,
        String? hintText,
        TextInputType keyboardType = TextInputType.text,
        List<TextInputFormatter>? inputFormatters,
        VoidCallback? onTap,
      }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SizedBox(
          height: 56,
          child: TextField(
            controller: controller,
            readOnly: onTap != null,
            onTap: onTap,
            keyboardType: keyboardType,
            inputFormatters: inputFormatters,
            decoration: InputDecoration(
              hintText: hintText,
              contentPadding: const EdgeInsets.symmetric(horizontal: 12),
              border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
              errorText: _errors[fieldName],
            ),
          ),
        ),
        const SizedBox(height: 4),
      ],
    );
  }

  static const TextStyle _labelStyle = TextStyle(
    fontFamily: "Raleway",
    fontWeight: FontWeight.bold,
    fontSize: 16,
  );

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: const CommonAppBar(title: 'Update Payment'),
      body: SingleChildScrollView(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text("Payment Method:", style: _labelStyle),
            const SizedBox(height: 6),
            TextField(
              controller: methodController,
              readOnly: true,
              style: const TextStyle(color: Colors.black87, fontWeight: FontWeight.w500),
              decoration: InputDecoration(
                filled: true,
                fillColor: Colors.grey.shade200,
                contentPadding: const EdgeInsets.symmetric(horizontal: 12),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
              ),
            ),
            const SizedBox(height: 20),
            const Text("Paypal Account Name:", style: _labelStyle),
            const SizedBox(height: 6),
            _buildInput(nameController, fieldName: "name"),
            const SizedBox(height: 20),
            const Text("Paypal Email:", style: _labelStyle),
            const SizedBox(height: 6),
            _buildInput(paypalEmailController, fieldName: "paypalEmail"),
            const SizedBox(height: 30),
            Center(
              child: ElevatedButton(
                onPressed: _handleUpdate,
                style: ElevatedButton.styleFrom(
                  backgroundColor: Color(0xFF013171),
                  padding: const EdgeInsets.symmetric(horizontal: 40, vertical: 12),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                ),
                child: const Text(
                  "Save",
                  style: TextStyle(fontSize: 17, color: Colors.white, fontFamily: 'Lexend'),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
