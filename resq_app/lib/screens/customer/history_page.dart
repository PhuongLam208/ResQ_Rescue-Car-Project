import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:resq_app/widgets/common_app_bar.dart';

class RescueTrip {
  final String type;
  final DateTime date;
  final double amount;
  final String iconAsset;
  final String id;
  // Thêm các thuộc tính chi tiết khác nếu có
  final String customerName; // Ví dụ
  final String customerPhone; // Ví dụ
  final String address; // Ví dụ
  final String serviceDescription; // Ví dụ

  RescueTrip({
    required this.type,
    required this.date,
    required this.amount,
    required this.iconAsset,
    required this.id,
    this.customerName = 'N/A', // Giá trị mặc định
    this.customerPhone = 'N/A',
    this.address = 'N/A',
    this.serviceDescription = 'No description available',
  });
}

class HistoryPage extends StatelessWidget {
  const HistoryPage({super.key});

  List<RescueTrip> get _recentTrips => [
    RescueTrip(
      type: 'On-site Rescue',
      date: DateTime(2025, 3, 15),
      amount: 625000,
      iconAsset: 'assets/icons/rescue_on_site.png',
      id: 'trip_001',
      customerName: 'Nguyen Van A',
      customerPhone: '0901234567',
      address: '123 Le Loi Street, Dist 1, HCMC',
      serviceDescription: 'Battery jump start and tire change.',
    ),
    RescueTrip(
      type: 'Driver Replacement',
      date: DateTime(2025, 3, 6),
      amount: 150000,
      iconAsset: 'assets/icons/driver_replacement.png',
      id: 'trip_002',
      customerName: 'Tran Thi B',
      customerPhone: '0912345678',
      address: '456 Tran Hung Dao, Dist 5, HCMC',
      serviceDescription: 'Driver replacement service from bar to home.',
    ),
    RescueTrip(
      type: 'Driver Replacement',
      date: DateTime(2025, 3, 3),
      amount: 52000,
      iconAsset: 'assets/icons/driver_replacement.png',
      id: 'trip_003',
      customerName: 'Le Van C',
      customerPhone: '0987654321',
      address: '789 Nguyen Trai Street, Dist 1, HCMC',
      serviceDescription: 'Short distance driver replacement.',
    ),
  ];

  List<RescueTrip> get _past3MonthsTrips => [
    ..._recentTrips, // Bao gồm các chuyến gần đây vào danh sách 3 tháng qua
    RescueTrip(
      type: 'Towing Truck',
      date: DateTime(2025, 2, 3),
      amount: 252000,
      iconAsset: 'assets/icons/towing_truck.png',
      id: 'trip_004',
      customerName: 'Pham Quoc D',
      customerPhone: '0965432109',
      address: '10 Hai Ba Trung, Binh Thanh Dist, HCMC',
      serviceDescription: 'Vehicle broke down, needed towing to garage.',
    ),
  ];

  String _formatCurrency(double amount) {
    final format = NumberFormat.currency(
      locale: 'vi_VN', // Keeping Vietnamese locale for currency format
      symbol: '',
      decimalDigits: 0,
    );
    return format.format(amount);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF8F8F8),
      appBar: const CommonAppBar(title: 'Rescue History'),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildSectionTitle('Recent'),
              _buildTripList(context, _recentTrips),
              const SizedBox(height: 24),
              _buildSectionTitle('Past 3 Months'),
              _buildTripList(context, _past3MonthsTrips),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0),
      child: Text(
        title,
        style: const TextStyle(
          fontSize: 16,
          fontWeight: FontWeight.bold,
          color: Colors.black87,
        ),
      ),
    );
  }

  Widget _buildTripList(BuildContext context, List<RescueTrip> trips) {
    return ListView.separated(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      itemCount: trips.length,
      separatorBuilder: (context, index) => const SizedBox(height: 12),
      itemBuilder: (context, index) => _buildTripCard(context, trips[index]),
    );
  }

  Widget _buildTripCard(BuildContext context, RescueTrip trip) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.black12,
            blurRadius: 6,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: const Color(0xFFE6E6FA),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Image.asset(
                trip.iconAsset,
                width: 30,
                height: 30,
                color: const Color(0xFF440196),
                errorBuilder: (context, error, stackTrace) {
                  return const Icon(
                    Icons.broken_image,
                    size: 30,
                    color: Colors.red,
                  );
                },
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    trip.type,
                    style: const TextStyle(
                      fontWeight: FontWeight.w600,
                      fontSize: 14,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: 4),
                  Text(
                    DateFormat('dd/MM/yyyy').format(trip.date),
                    style: const TextStyle(color: Colors.grey, fontSize: 13),
                  ),
                ],
              ),
            ),
            Column(
              crossAxisAlignment: CrossAxisAlignment.end,
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text(
                  '${_formatCurrency(trip.amount)} VNĐ',
                  style: const TextStyle(
                    fontWeight: FontWeight.bold,
                    color: Color(0xFF440196),
                    fontSize: 13,
                  ),
                ),
                SizedBox(
                  height: 25,
                  child: TextButton(
                    onPressed: () {
                      _showTripDetailsDialog(
                        context,
                        trip,
                      ); // <-- Gọi hàm hiển thị dialog
                    },
                    style: TextButton.styleFrom(
                      padding: EdgeInsets.zero,
                      minimumSize: const Size(60, 20),
                      tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                    ),
                    child: const Text(
                      'Details',
                      style: TextStyle(
                        color: Colors.blue,
                        fontSize: 12,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  // Hàm hiển thị dialog chi tiết chuyến cứu hộ
  void _showTripDetailsDialog(BuildContext context, RescueTrip trip) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(15),
          ),
          title: Text(
            'Rescue Trip Details (ID: ${trip.id})',
            style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18),
          ),
          content: SingleChildScrollView(
            // Để nội dung có thể cuộn nếu dài
            child: ListBody(
              children: <Widget>[
                _buildDetailRow('Type:', trip.type),
                _buildDetailRow(
                  'Date:',
                  DateFormat('dd/MM/yyyy HH:mm').format(trip.date),
                ),
                _buildDetailRow(
                  'Amount:',
                  '${_formatCurrency(trip.amount)} VNĐ',
                ),
                _buildDetailRow('Customer Name:', trip.customerName),
                _buildDetailRow('Customer Phone:', trip.customerPhone),
                _buildDetailRow('Address:', trip.address),
                _buildDetailRow('Description:', trip.serviceDescription),
                // Thêm các chi tiết khác tại đây
              ],
            ),
          ),
          actions: <Widget>[
            TextButton(
              child: const Text(
                'Close',
                style: TextStyle(color: Color(0xFF440196)),
              ),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }

  // Widget tiện ích để tạo một hàng chi tiết trong dialog
  Widget _buildDetailRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4.0),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 100, // Đặt chiều rộng cố định cho nhãn để căn chỉnh tốt hơn
            child: Text(
              label,
              style: const TextStyle(
                fontWeight: FontWeight.w600,
                color: Colors.black87,
              ),
            ),
          ),
          Expanded(
            child: Text(value, style: const TextStyle(color: Colors.black54)),
          ),
        ],
      ),
    );
  }
}
