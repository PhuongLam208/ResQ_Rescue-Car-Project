// lib/pages/confirm_request_page.dart
import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';
import 'package:resq_app/models/rescue_response.dart';

// Import các trang và model mới
import 'package:resq_app/pages/discount_page.dart';
import 'package:resq_app/pages/payment_method_page.dart';
import 'package:resq_app/models/discount_code.dart';
import 'package:resq_app/pages/waiting_partner_page.dart';
import 'package:resq_app/services/payment_service.dart';
import 'package:resq_app/services/rescue_api_service.dart'; // Import DiscountCode

class ConfirmRequestPage extends StatefulWidget {
  final LatLng startLocation;
  final LatLng? destinationLocation;
  final List<LatLng>? routePoints;
  final double distanceKm;
  final String rescueType;
  final int rrid;
  final int userId;
  final RescueResponse requestResult;
  final String startAddress;
  final String? destinationAddress;
  final String estimatedTime;

  const ConfirmRequestPage({
    Key? key,
    required this.startLocation,
    this.destinationLocation,
    this.routePoints,
    required this.distanceKm,
    required this.rescueType,
    required this.rrid,
    required this.userId,
    
    required this.requestResult,
    required this.startAddress,
    this.destinationAddress,
    required this.estimatedTime,
  }) : super(key: key);

  @override
  State<ConfirmRequestPage> createState() => _ConfirmRequestPageState();
}

class _ConfirmRequestPageState extends State<ConfirmRequestPage> {
  static const primaryColor = Color(0xFF062D4E);
  static const lightColor = Color(0xFFEAF3FA);
  static const accentColor = Color(0xFF2980B9);

  String? _selectedPaymentMethod;
  DiscountCode? _appliedDiscount; // Lưu trữ đối tượng DiscountCode đã áp dụng

  // Bỏ danh sách _userDiscountCodes ở đây nếu bạn muốn nó hoàn toàn tĩnh trong DiscountPage
  // final List<DiscountCode> _userDiscountCodes = [...];

  @override
  void initState() {
    super.initState();
    _selectedPaymentMethod = widget.requestResult.method; // Set initial method
  }

  // Hàm tính toán tổng tiền cuối cùng
  double _calculateFinalTotal() {
    double total = widget.requestResult.total;
    if (_appliedDiscount != null) {
      total -= _appliedDiscount!.amount;
    }
    return total > 0 ? total : 0.0; // Đảm bảo tổng tiền không âm
  }

  @override
  Widget build(BuildContext context) {
    final bill = widget.requestResult;
    final initialFocus = widget.destinationLocation ?? widget.startLocation;
    final finalTotal = _calculateFinalTotal();

    return Scaffold(
      backgroundColor: Colors.grey.shade100,
      appBar: AppBar(
        title: const Text(
          "Confirm Rescue Request",
          style: TextStyle(color: Colors.white, fontWeight: FontWeight.w600),
        ),
        backgroundColor: primaryColor,
        iconTheme: const IconThemeData(color: Colors.white),
        elevation: 0,
      ),
      body: Column(
        children: [
          // MAP SECTION (Giữ nguyên)
          Expanded(
            flex: 3,
            child: Container(
              decoration: const BoxDecoration(
                boxShadow: [
                  BoxShadow(
                    color: Colors.black12,
                    blurRadius: 8,
                    offset: Offset(0, 4),
                  ),
                ],
              ),
              child: Stack(
                children: [
                  FlutterMap(
                    options: MapOptions(
                      initialCenter: initialFocus,
                      initialZoom: 14,
                    ),
                    children: [
                      TileLayer(
                        urlTemplate:
                            'https://api.mapbox.com/styles/v1/mapbox/streets-v12/tiles/256/{z}/{x}/{y}@2x?access_token=pk.eyJ1IjoidHJhbXRyYW4xMjMiLCJhIjoiY21kNGRkMHQ0MGY2NTJscjZmcDY4bzVuNCJ9.L4-zGwpDVXx9aKqTqbDyvA',
                        userAgentPackageName: 'com.example.resq_app',
                      ),
                      MarkerLayer(
                        markers: [
                          Marker(
                            point: widget.startLocation,
                            width: 40,
                            height: 40,
                            child: const Icon(
                              Icons.my_location,
                              color: Colors.green,
                              size: 32,
                            ),
                          ),
                          if (widget.destinationLocation != null)
                            Marker(
                              point: widget.destinationLocation!,
                              width: 40,
                              height: 40,
                              child: const Icon(
                                Icons.location_on,
                                color: primaryColor,
                                size: 32,
                              ),
                            ),
                        ],
                      ),
                      if (widget.routePoints != null &&
                          widget.routePoints!.isNotEmpty)
                        PolylineLayer(
                          polylines: [
                            Polyline(
                              points: widget.routePoints!,
                              strokeWidth: 4,
                              color: accentColor,
                            ),
                          ],
                        ),
                    ],
                  ),
                  Positioned(
                    top: 16,
                    left: 16,
                    child: Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 14,
                        vertical: 8,
                      ),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(20),
                        boxShadow: const [
                          BoxShadow(
                            color: Colors.black26,
                            blurRadius: 8,
                            offset: Offset(0, 2),
                          ),
                        ],
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          const Icon(
                            Icons.route,
                            size: 14,
                            color: primaryColor,
                          ),
                          const SizedBox(width: 6),
                          Text(
                            '${widget.distanceKm.toStringAsFixed(1)} km • ${widget.estimatedTime}',
                            style: const TextStyle(
                              fontSize: 13,
                              fontWeight: FontWeight.w600,
                              color: primaryColor,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),

          // INFO SECTION
          Expanded(
            flex: 4,
            child: Container(
              width: double.infinity,
              padding: const EdgeInsets.fromLTRB(24, 28, 24, 24),
              decoration: const BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
              ),
              child: SingleChildScrollView(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _buildSectionTitle("Request Details"),
                    const SizedBox(height: 16),
                    _buildInfoCard([
                      _InfoItem(
                        icon: Icons.build,
                        label: "Rescue Type",
                        value: widget.rescueType,
                      ),
                      _InfoItem(
                        icon: Icons.location_on,
                        label: "From",
                        value: widget.startAddress,
                      ),
                      if (widget.destinationAddress != null &&
                          widget.destinationAddress!.isNotEmpty)
                        _InfoItem(
                          icon: Icons.flag,
                          label: "To",
                          value: widget.destinationAddress!,
                        ),
                    ]),
                    const SizedBox(height: 28),

                    // Phần Payment Information - Điều hướng đến trang riêng
                    _buildSectionTitle("Payment Information"),
                    const SizedBox(height: 16),
                    _buildClickableInfoCard(
                      icon: Icons.payment,
                      label: "Payment Method",
                      value: _selectedPaymentMethod ?? 'Select Method',
                      onTap: () async {
                        final result = await Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder:
                                (context) => PaymentMethodPage(
                                  currentSelectedMethod: _selectedPaymentMethod,
                                ),
                          ),
                        );
                        if (result != null && result is String) {
                          setState(() {
                            _selectedPaymentMethod = result;
                          });
                        }
                      },
                    ),
                    const SizedBox(height: 20),

                    // Phần Discount Code - Điều hướng đến trang riêng (không truyền dữ liệu)
                    _buildSectionTitle("Discount Code"),
                    const SizedBox(height: 16),
                    _buildClickableInfoCard(
                      icon: Icons.local_offer,
                      label: "Discount",
                      value:
                          _appliedDiscount != null
                              ? '-${_appliedDiscount!.amount.toStringAsFixed(0)} ${bill.currency} (${_appliedDiscount!.code})'
                              : 'No discount applied',
                      onTap: () async {
                        // Gọi DiscountPage mà không cần truyền availableDiscountCodes hay currentSelectedDiscount
                        final result = await Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder:
                                (context) => DiscountPage(
                                  userId: widget.userId,
                                ), // Gọi constructor mặc định
                          ),
                        );
                        if (result != null && result is DiscountCode) {
                          setState(() {
                            _appliedDiscount = result;
                          });
                        } else if (result == null) {
                          setState(() {
                            _appliedDiscount = null;
                          });
                        }
                      },
                    ),
                    const SizedBox(height: 20),

                    // Total Amount
                    _buildTotalAmount(finalTotal, bill.currency),
                    if (_appliedDiscount != null)
                      Padding(
                        padding: const EdgeInsets.only(top: 8.0),
                        child: Align(
                          alignment: Alignment.centerRight,
                          child: Text(
                            "Original Total: ${widget.requestResult.total.toStringAsFixed(0)} ${bill.currency}",
                            style: const TextStyle(
                              fontSize: 13,
                              fontWeight: FontWeight.w500,
                              color: Colors.grey,
                              decoration: TextDecoration.lineThrough,
                            ),
                          ),
                        ),
                      ),
                    const SizedBox(height: 24),
                    _buildConfirmButton(context),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  // Các hàm build helper khác (buildClickableInfoCard, buildSectionTitle, buildInfoCard, buildTotalAmount, buildConfirmButton)
  // và class _InfoItem giữ nguyên như đã cung cấp trước đó.
  // ... (Phần còn lại của ConfirmRequestPage không thay đổi) ...

  void showSnackBar(BuildContext context, String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: Colors.red,
        duration: const Duration(seconds: 3),
      ),
    );
  }

  Widget _buildClickableInfoCard({
    required IconData icon,
    required String label,
    required String value,
    required VoidCallback onTap,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.grey[50],
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: Colors.grey[200]!),
        ),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            Icon(icon, size: 18, color: primaryColor),
            const SizedBox(width: 10),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    label,
                    style: const TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w500,
                      color: Colors.grey,
                    ),
                  ),
                  const SizedBox(height: 3),
                  Text(
                    value,
                    style: const TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                      color: Colors.black87,
                      height: 1.3,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                ],
              ),
            ),
            const Icon(Icons.arrow_forward_ios, size: 16, color: Colors.grey),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          width: 4,
          height: 20,
          decoration: BoxDecoration(
            color: primaryColor,
            borderRadius: BorderRadius.circular(2),
          ),
        ),
        const SizedBox(width: 12),
        Text(
          title,
          style: const TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
            color: primaryColor,
          ),
        ),
      ],
    );
  }

  Widget _buildInfoCard(List<_InfoItem> items) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.grey[50],
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.grey[200]!),
      ),
      child: Column(
        children:
            items
                .map(
                  (item) => Padding(
                    padding: const EdgeInsets.symmetric(vertical: 8),
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Icon(item.icon, size: 18, color: primaryColor),
                        const SizedBox(width: 10),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                item.label,
                                style: const TextStyle(
                                  fontSize: 12,
                                  fontWeight: FontWeight.w500,
                                  color: Colors.grey,
                                ),
                              ),
                              const SizedBox(height: 3),
                              Text(
                                item.value,
                                style: const TextStyle(
                                  fontSize: 14,
                                  fontWeight: FontWeight.w600,
                                  color: Colors.black87,
                                  height: 1.3,
                                ),
                                maxLines: 2,
                                overflow: TextOverflow.ellipsis,
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                )
                .toList(),
      ),
    );
  }

  Widget _buildTotalAmount(double amount, String currency) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: lightColor,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: primaryColor.withOpacity(0.2)),
      ),
      child: Column(
        children: [
          const Text(
            "Total Cost",
            style: TextStyle(
              fontSize: 13,
              color: Colors.grey,
              fontWeight: FontWeight.w500,
            ),
          ),
          const SizedBox(height: 8),
          FittedBox(
            fit: BoxFit.scaleDown,
            child: Text(
              "${amount.toStringAsFixed(0)} $currency",
              style: const TextStyle(
                fontSize: 22,
                fontWeight: FontWeight.bold,
                color: primaryColor,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildConfirmButton(BuildContext context) {
    return Container(
      width: double.infinity,
      height: 54,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(16),
        gradient: const LinearGradient(
          colors: [primaryColor, accentColor],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        boxShadow: [
          BoxShadow(
            color: primaryColor.withOpacity(0.3),
            blurRadius: 12,
            offset: const Offset(0, 6),
          ),
        ],
      ),
      child: ElevatedButton(
        onPressed: () async {
          showDialog(
            context: context,
            barrierDismissible: false,
            builder: (_) => const Center(child: CircularProgressIndicator()),
          );

          try {
            if (_selectedPaymentMethod == "Paypal") {
              final orderId = await PaymentService.createPayment(
                widget.requestResult.rrid,
              );

              if (orderId != null) {
                print("Created orderId: $orderId");

                Timer.periodic(const Duration(seconds: 30), (timer) async {
                  final status = await PaymentService.capturePayment(orderId);
                  print("Check Status: $status");

                  if (status == "COMPLETED") {
                    timer.cancel();
                    Navigator.of(context).pop(); // Đóng loading

                    Navigator.pushReplacement(
                      context,
                      MaterialPageRoute(
                        builder:
                            (_) => WaitingPartnerPage(
                              rrid: widget.requestResult.rrid,
                              startLocation: widget.startLocation,
                              destinationLocation: widget.destinationLocation,
                              routePoints: widget.routePoints,
                            ),
                      ),
                    );
                  } else if (status == "FAILED" || status == "VOIDED") {
                    timer.cancel();
                    Navigator.of(context).pop(); // Đóng loading

                    showDialog(
                      context: context,
                      barrierDismissible: false,
                      builder:
                          (_) => AlertDialog(
                            title: const Text("Payment Failed"),
                            content: const Text(
                              "The payment was not successful. Please try again.",
                            ),
                            actions: [
                              TextButton(
                                onPressed: () {
                                  Navigator.pushNamedAndRemoveUntil(
                                    context,
                                    '/homepage',
                                    (route) => false,
                                  );
                                },
                                child: const Text("Đóng"),
                              ),
                            ],
                          ),
                    );
                  }
                });
              } else {
                Navigator.of(context).pop(); // Đóng loading
                showSnackBar(context, "Không thể tạo đơn hàng");
              }
            } else {
              Navigator.of(context).pop(); // Đóng loading nếu chọn tiền mặt

              Navigator.pushReplacement(
                context,
                MaterialPageRoute(
                  builder:
                      (_) => WaitingPartnerPage(
                        rrid: widget.requestResult.rrid,
                        startLocation: widget.startLocation,
                        destinationLocation: widget.destinationLocation,
                        routePoints: widget.routePoints,
                      ),
                ),
              );
            }
          } catch (e) {
            Navigator.of(context).pop(); // Đóng loading

            showSnackBar(context, "Lỗi: $e");
          }
        },
        style: ElevatedButton.styleFrom(
          backgroundColor: Colors.transparent,
          shadowColor: Colors.transparent,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
          ),
        ),
        child: const Text(
          "Confirm Rescue",
          style: TextStyle(
            fontSize: 15,
            fontWeight: FontWeight.w600,
            color: Colors.white,
          ),
        ),
      ),
    );
  }
}

class _InfoItem {
  final IconData icon;
  final String label;
  final String value;

  _InfoItem({required this.icon, required this.label, required this.value});
}
