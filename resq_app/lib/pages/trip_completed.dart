import 'package:flutter/material.dart';
import 'package:resq_app/models/rescue_response.dart';
import 'package:resq_app/screens/customer/customer_feedback.dart';

class TripCompletedScreen extends StatelessWidget {
  final RescueResponse? tripData;

  const TripCompletedScreen({super.key, this.tripData});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'Chuyến đi đã hoàn thành',
          style: TextStyle(color: Colors.white),
        ),
        backgroundColor: Color.fromARGB(255, 7, 51, 86), // Màu sắc đồng bộ
        iconTheme: const IconThemeData(color: Colors.white),
        centerTitle: true,
        automaticallyImplyLeading: false, // Tắt nút back mặc định trên AppBar
      ),
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            children: [
              // Icon thành công
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: Color.fromARGB(
                    255,
                    7,
                    51,
                    86,
                  ).withOpacity(0.1), // Nền nhạt hơn
                  shape: BoxShape.circle,
                ),
                child: const Icon(
                  Icons.check_circle,
                  color: Color.fromARGB(
                    255,
                    7,
                    51,
                    86,
                  ), // Màu icon khớp với theme
                  size: 80,
                ),
              ),
              const SizedBox(height: 20),
              const Text(
                'Chuyến đi đã hoàn thành thành công!',
                style: TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                  color: Color.fromARGB(255, 7, 51, 86),
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 30),
              // Thẻ chi tiết chuyến đi
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(16),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.1),
                      blurRadius: 10,
                      offset: const Offset(0, 4),
                    ),
                  ],
                ),
                child: Column(
                  children: [
                    _buildDetailRow(
                      'ID Cứu hộ',
                      tripData?.rrid.toString() ?? '-',
                    ),
                    _buildDetailRow(
                      'Dịch vụ',
                      tripData?.breakdown?.serviceName ?? '-',
                    ),
                    _buildDetailRow(
                      'Tài xế',
                      tripData?.breakdown?.driverName ?? '-',
                    ),
                    _buildDetailRow(
                      'Tổng cộng',
                      '${tripData?.total?.toStringAsFixed(0) ?? '-'} ${tripData?.currency ?? 'VNĐ'}',
                    ),
                    _buildDetailRow(
                      'Phương thức thanh toán',
                      tripData?.method ?? '-',
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 30),
              // Các nút hành động
              Row(
                children: [
                  Expanded(
                    child: ElevatedButton(
                      onPressed: () {
                        // Điều hướng đến màn hình CustomerFeedbackScreen
                        // Sử dụng pushReplacement để tránh người dùng quay lại TripCompletedScreen từ Feedback
                        Navigator.pushReplacement(
                          context,
                          MaterialPageRoute(
                            builder:
                                (context) => CustomerFeedbackScreen(
                                  rrid: tripData?.rrid ?? 0,
                                ),
                          ),
                        );
                      },
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.blue, // Màu nút Rate Service
                        padding: const EdgeInsets.symmetric(vertical: 15),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(10),
                        ),
                      ),
                      child: const Text(
                        'Đánh giá Dịch vụ',
                        style: TextStyle(color: Colors.white),
                      ),
                    ),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: ElevatedButton(
                      onPressed: () {
                        // Quay về màn hình Home và xóa tất cả các màn hình khác khỏi stack
                        Navigator.popUntil(context, (route) => route.isFirst);
                        // Hoặc nếu màn hình Home của bạn có tên route, bạn có thể dùng:
                        // Navigator.pushNamedAndRemoveUntil(context, '/home', (route) => false);
                        // (Bạn cần định nghĩa route '/home' trong MaterialApp của mình)
                      },
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Color.fromARGB(
                          255,
                          7,
                          51,
                          86,
                        ), // Màu nút Back to Home
                        padding: const EdgeInsets.symmetric(vertical: 15),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(10),
                        ),
                      ),
                      child: const Text(
                        'Về Trang Chủ',
                        style: TextStyle(color: Colors.white),
                      ),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  // Helper cho hàng chi tiết
  Widget _buildDetailRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(fontSize: 14, color: Colors.grey)),
          Text(
            value,
            style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w600),
          ),
        ],
      ),
    );
  }
}
