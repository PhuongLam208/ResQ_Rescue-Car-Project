import 'package:flutter/material.dart';
import 'package:resq_app/models/discount_code.dart';
import 'package:resq_app/services/rescue_api_service.dart';

class DiscountPage extends StatefulWidget {
  final int userId;

  const DiscountPage({Key? key, required this.userId}) : super(key: key);

  @override
  State<DiscountPage> createState() => _DiscountPageState();
}

class _DiscountPageState extends State<DiscountPage> {
  static const primaryColor = Color(0xFF062D4E);

  final RescueApiService _apiService = RescueApiService();
  List<DiscountCode> _availableDiscountCodes = [];
  DiscountCode? _selectedDiscount;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadDiscountCodes();
  }

  Future<void> _loadDiscountCodes() async {
    try {
      final discounts = await RescueApiService.getCustomerDiscount(widget.userId);
      setState(() {
        _availableDiscountCodes = discounts.cast<DiscountCode>();
      });
    } catch (e) {
      print('Error fetching discounts: $e');
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  String formatAmount(DiscountCode d) {
    return d.type == "Percent"
        ? "${d.amount.toStringAsFixed(0)}%"
        : "${d.amount.toStringAsFixed(0)}đ";
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          "Select Discount Code",
          style: TextStyle(color: Colors.white, fontWeight: FontWeight.w600),
        ),
        backgroundColor: primaryColor,
        iconTheme: const IconThemeData(color: Colors.white),
        elevation: 0,
      ),
      body:
          _isLoading
              ? const Center(child: CircularProgressIndicator())
              : Column(
                children: [
                  Expanded(
                    child:
                        _availableDiscountCodes.isEmpty
                            ? const Center(
                              child: Text(
                                "You don't have any discount codes.",
                                style: TextStyle(
                                  fontSize: 16,
                                  color: Colors.grey,
                                ),
                              ),
                            )
                            : ListView.builder(
                              padding: const EdgeInsets.all(16.0),
                              itemCount: _availableDiscountCodes.length,
                              itemBuilder: (context, index) {
                                final discount = _availableDiscountCodes[index];
                                return Card(
                                  margin: const EdgeInsets.symmetric(
                                    vertical: 8.0,
                                  ),
                                  elevation: 2,
                                  shape: RoundedRectangleBorder(
                                    borderRadius: BorderRadius.circular(12),
                                  ),
                                  child: RadioListTile<DiscountCode>(
                                    title: Text(
                                      discount.code,
                                      style: const TextStyle(
                                        fontSize: 16,
                                        fontWeight: FontWeight.w600,
                                        color: Colors.black87,
                                      ),
                                    ),
                                    subtitle: Text(
                                      '${discount.name} - ${formatAmount(discount)}',
                                      style: const TextStyle(
                                        fontSize: 13,
                                        color: Colors.grey,
                                      ),
                                    ),
                                    value: discount,
                                    groupValue: _selectedDiscount,
                                    onChanged: (DiscountCode? value) {
                                      setState(() {
                                        _selectedDiscount = value;
                                      });
                                    },
                                    secondary: Icon(
                                      Icons.local_offer,
                                      color: primaryColor,
                                    ),
                                    controlAffinity:
                                        ListTileControlAffinity.trailing,
                                    activeColor: primaryColor,
                                    tileColor:
                                        _selectedDiscount == discount
                                            ? primaryColor.withOpacity(0.05)
                                            : null,
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
                            _selectedDiscount == null
                                ? null
                                : () {
                                  Navigator.pop(context, _selectedDiscount);
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
                          "Apply Discount",
                          style: TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    ),
                  ),
                  if (_selectedDiscount != null)
                    Padding(
                      padding: const EdgeInsets.only(bottom: 16.0),
                      child: TextButton(
                        onPressed: () {
                          Navigator.pop(context, null);
                        },
                        child: const Text(
                          "Remove Discount",
                          style: TextStyle(color: Colors.red, fontSize: 14),
                        ),
                      ),
                    ),
                ],
              ),
    );
  }
}
