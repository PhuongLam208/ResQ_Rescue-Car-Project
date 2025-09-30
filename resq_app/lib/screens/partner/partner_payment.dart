import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:resq_app/services/api_result.dart';
import 'package:resq_app/services/partner_service.dart';
import 'package:resq_app/widgets/common_app_bar.dart';

class PartnerPaymentScreen extends StatefulWidget {
  final int rrid;
  final int partnerId;
  final String paymentMethod;
  const PartnerPaymentScreen({
    super.key,
    required this.rrid,
    required this.partnerId,
    required this.paymentMethod,
  });

  @override
  State<PartnerPaymentScreen> createState() => _PartnerPaymentScreenState();
}

class _PartnerPaymentScreenState extends State<PartnerPaymentScreen> {
  double rescueAmount = 740000;
  double appFee = 74000;
  bool isLoading = true;

  final Color tagColor = const Color(0xFF013171);

  @override
  void initState() {
    super.initState();
    _fetchPaymentAmount();
  }

  Future<void> _fetchPaymentAmount() async {
    ApiResult result = await PartnerService().getPaymentAmount(widget.rrid);
    if (result.statusCode == 200) {
      final data = jsonDecode(result.body);
      setState(() {
        rescueAmount = (data['totalPrice'] ?? 0).toDouble();
        appFee = (data['appFee'] ?? 0).toDouble();
        isLoading = false;
      });
    } else {
      setState(() {
        isLoading = false;
      });
    }
  }

  Future<void> _receiveMoney() async {
    double totalReceived;
    if (widget.paymentMethod == ("CASH")) {
      totalReceived = appFee;
    } else {
      totalReceived = rescueAmount - appFee;
    }
    ApiResult result = await PartnerService().receiveMoney(
      partnerId: widget.partnerId,
      totalReceived: totalReceived,
      paymentMethod: widget.paymentMethod,
    );

    if (result.statusCode == 200) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Money received successfully")),
      );
      // You can navigate or refresh here if needed
    } else {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text("Failed: ${result.body}")));
    }
  }

  @override
  Widget build(BuildContext context) {
    double totalReceived = rescueAmount - appFee;

    return Scaffold(
      appBar: const CommonAppBar(title: 'Trip Summary'),
      body:
          isLoading
              ? const Center(child: CircularProgressIndicator())
              : Padding(
                padding: const EdgeInsets.symmetric(
                  horizontal: 48,
                  vertical: 20,
                ),
                child: Column(
                  children: [
                    const SizedBox(height: 40),
                    const Text(
                      "You have completed the Rescue Trip!",
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                        color: Color(0xFF013171),
                      ),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 24),

                    // Fee breakdown
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        _buildRow("Rescue amount:", rescueAmount),
                        _buildRow("App fee:", appFee),
                        _buildRow("Total received:", totalReceived, bold: true),
                      ],
                    ),

                    const SizedBox(height: 24),
                    const Text(
                      "The money will be automatically credited to your wallet in the app. "
                      "Go to the Home Partner screen to check and withdraw!",
                      style: TextStyle(
                        color: Color(0xFF013171),
                        fontSize: 14,
                        fontStyle: FontStyle.italic,
                      ),
                      textAlign: TextAlign.center,
                    ),

                    const SizedBox(height: 30),
                    ElevatedButton(
                      onPressed: _receiveMoney,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: const Color(0xFFBB0000),
                        padding: const EdgeInsets.symmetric(
                          horizontal: 40,
                          vertical: 12,
                        ),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(10),
                        ),
                      ),
                      child: const Text(
                        "Receive Money",
                        style: TextStyle(color: Colors.white),
                      ),
                    ),
                  ],
                ),
              ),
    );
  }

  Widget _buildRow(String label, double value, {bool bold = false}) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Row(
        children: [
          Expanded(
            child: Text(
              label,
              style: TextStyle(
                fontSize: 16,
                fontWeight: bold ? FontWeight.bold : FontWeight.normal,
                color: tagColor,
              ),
            ),
          ),
          Text(
            "${value.toStringAsFixed(0)} VND",
            style: TextStyle(
              fontSize: 16,
              fontWeight: bold ? FontWeight.bold : FontWeight.normal,
              color: tagColor,
            ),
          ),
        ],
      ),
    );
  }
}
