import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart'; // Import thư viện biểu đồ
import 'package:intl/intl.dart'; // Để định dạng ngày tháng

class StatisticsPage extends StatelessWidget {
  const StatisticsPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF8F8F8),
      appBar: AppBar(
        backgroundColor: const Color(0xFF440196), // Màu tím đậm
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios, color: Colors.white),
          onPressed: () {
            Navigator.pop(context);
          },
        ),
        title: const Text(
          'Statistics',
          style: TextStyle(
            color: Colors.white,
            fontWeight: FontWeight.bold,
            fontSize: 18,
          ),
        ),
        centerTitle: true,
        elevation: 2,
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildUserProfileSection(),
              const SizedBox(height: 24),
              _buildStatisticsChart(),
              const SizedBox(height: 24),
              _buildReviewSection(
                context,
              ), // Truyền context vào _buildReviewSection
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildUserProfileSection() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.black12.withOpacity(0.05),
            blurRadius: 8,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Row(
        children: [
          // Placeholder for avatar (assuming 'An Vo' is a user name)
          CircleAvatar(
            radius: 30,
            backgroundColor: Colors.grey.shade200,
            child: Icon(Icons.person, size: 35, color: Colors.grey.shade600),
          ),
          const SizedBox(width: 16),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'An Vo', // Example User Name
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  color: Colors.black87,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                'Date: ${DateFormat('dd/MM/yyyy').format(DateTime(2023, 3, 3))}', // Example Date
                style: const TextStyle(fontSize: 13, color: Colors.grey),
              ),
            ],
          ),
          const Spacer(), // Pushes content to the right
          SizedBox(
            width: 120, // Adjust width for the chart area
            height: 100, // Adjust height for the chart area
            child: BarChart(
              BarChartData(
                alignment: BarChartAlignment.spaceBetween,
                maxY: 25, // Based on image's highest value (24)
                barTouchData: BarTouchData(
                  enabled: false,
                ), // Disable touch for simplicity
                titlesData: FlTitlesData(
                  show: true,
                  rightTitles: const AxisTitles(
                    sideTitles: SideTitles(showTitles: false),
                  ),
                  topTitles: const AxisTitles(
                    sideTitles: SideTitles(showTitles: false),
                  ),
                  bottomTitles: const AxisTitles(
                    sideTitles: SideTitles(showTitles: false),
                  ),
                  leftTitles: AxisTitles(
                    sideTitles: SideTitles(
                      showTitles: true,
                      interval: 5, // Interval for Y-axis labels
                      getTitlesWidget: (value, meta) {
                        return Text(
                          value.toInt().toString(),
                          style: const TextStyle(
                            fontSize: 10,
                            color: Colors.grey,
                          ),
                        );
                      },
                      reservedSize:
                          28, // <-- Nên có reservedSize cho leftTitles
                    ),
                  ),
                ),
                borderData: FlBorderData(
                  show: true,
                  border: Border.all(color: Colors.grey.shade300, width: 0.5),
                ),
                gridData: FlGridData(
                  show: true,
                  drawVerticalLine: false,
                  getDrawingHorizontalLine:
                      (value) => const FlLine(
                        color: Colors.grey,
                        strokeWidth: 0.5,
                        dashArray: [2, 2],
                      ),
                ),
                barGroups: [
                  BarChartGroupData(
                    x: 0,
                    barRods: [
                      BarChartRodData(
                        toY: 24, // Total complaints
                        color: Colors.blue.shade400,
                        width: 8,
                        borderRadius: BorderRadius.circular(2),
                      ),
                    ],
                  ),
                  BarChartGroupData(
                    x: 1,
                    barRods: [
                      BarChartRodData(
                        toY: 15, // Total completed complaints
                        color: Colors.lightBlue.shade300,
                        width: 8,
                        borderRadius: BorderRadius.circular(2),
                      ),
                    ],
                  ),
                  BarChartGroupData(
                    x: 2,
                    barRods: [
                      BarChartRodData(
                        toY: 10, // Total promotions used
                        color: Colors.purple.shade400,
                        width: 8,
                        borderRadius: BorderRadius.circular(2),
                      ),
                    ],
                  ),
                  BarChartGroupData(
                    x: 3,
                    barRods: [
                      BarChartRodData(
                        toY: 13, // Total spent in current year
                        color: Colors.deepPurple.shade300,
                        width: 8,
                        borderRadius: BorderRadius.circular(2),
                      ),
                    ],
                  ),
                  BarChartGroupData(
                    x: 4,
                    barRods: [
                      BarChartRodData(
                        toY: 20, // Total ratings
                        color: Colors.red.shade400,
                        width: 8,
                        borderRadius: BorderRadius.circular(2),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatisticsChart() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.black12.withOpacity(0.05),
            blurRadius: 8,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Overall Statistics',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.bold,
              color: Colors.black87,
            ),
          ),
          const SizedBox(height: 16),
          _buildStatRow('Total Complaints:', '24', Colors.blue.shade400),
          _buildStatRow(
            'Total Completed Complaints:',
            '15',
            Colors.lightBlue.shade300,
          ),
          _buildStatRow('Total Promotions Used:', '10', Colors.purple.shade400),
          _buildStatRow(
            'Total Spent This Year:',
            '13 (Trillion VND)',
            Colors.deepPurple.shade300,
          ),
          _buildStatRow('Total Ratings:', '20', Colors.red.shade400),
        ],
      ),
    );
  }

  Widget _buildStatRow(String label, String value, Color color) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: [
              Container(
                width: 12,
                height: 12,
                decoration: BoxDecoration(
                  color: color,
                  borderRadius: BorderRadius.circular(3),
                ),
              ),
              const SizedBox(width: 8),
              Text(
                label,
                style: const TextStyle(fontSize: 14, color: Colors.black87),
              ),
            ],
          ),
          Text(
            value,
            style: const TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.bold,
              color: Colors.black87,
            ),
          ),
        ],
      ),
    );
  }

  void _showInvoiceDialog(BuildContext context, String reviewTitle) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(15),
          ),
          title: Text(
            'Invoice Details for "$reviewTitle"',
            style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18),
          ),
          content: SingleChildScrollView(
            child: ListBody(
              children: <Widget>[
                Text(
                  'Invoice ID: INV-${DateTime.now().millisecondsSinceEpoch % 10000}',
                  style: const TextStyle(fontSize: 14),
                ),
                const SizedBox(height: 8),
                const Text(
                  'Service: Roadside Assistance',
                  style: TextStyle(fontSize: 14),
                ),
                const SizedBox(height: 8),
                Text(
                  'Date: ${DateFormat('dd/MM/yyyy HH:mm').format(DateTime.now())}',
                  style: const TextStyle(fontSize: 14),
                ),
                const SizedBox(height: 8),
                const Text(
                  'Amount: 500,000 VND',
                  style: TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.bold,
                    color: Colors.green,
                  ),
                ),
                const SizedBox(height: 15),
                const Text(
                  'Service Provider: XYZ Rescuer Team',
                  style: TextStyle(fontSize: 13, color: Colors.grey),
                ),
                const SizedBox(height: 5),
                const Text(
                  'Contact: 0912-345-678',
                  style: TextStyle(fontSize: 13, color: Colors.grey),
                ),
                const SizedBox(height: 15),
                const Text(
                  'Payment Method: Cash',
                  style: TextStyle(fontSize: 14),
                ),
                const SizedBox(height: 8),
                const Text(
                  'Status: Paid',
                  style: TextStyle(fontSize: 14, color: Colors.blue),
                ),
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
            // Đã bỏ nút "Download PDF" ở đây
            // TextButton(
            //   child: const Text('Download PDF', style: TextStyle(color: Colors.blue)),
            //   onPressed: () {
            //     ScaffoldMessenger.of(context).showSnackBar(
            //       const SnackBar(content: Text('Downloading invoice...')),
            //     );
            //     Navigator.of(context).pop();
            //   },
            // ),
          ],
        );
      },
    );
  }

  // Cần truyền BuildContext vào _buildReviewSection
  Widget _buildReviewSection(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'My Reviews',
          style: TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.bold,
            color: Colors.black87,
          ),
        ),
        const SizedBox(height: 16),
        _buildReviewCard(
          context, // Truyền context vào đây
          'Res Trip on ${DateFormat('dd/MM/yyyy').format(DateTime(2023, 3, 3))}', //
          'Comments: Professional, very enthusiastic rescuer', //
          4, // Example rating
        ),
        const SizedBox(height: 12),
        _buildReviewCard(
          context, // Truyền context vào đây
          'Res Trip on ${DateFormat('dd/MM/yyyy').format(DateTime(2023, 3, 3))}', //
          'Comments: Professional, very enthusiastic rescuer', //
          5, // Example rating
        ),
        const SizedBox(height: 12),
        _buildReviewCard(
          context, // Truyền context vào đây
          'Res Trip on ${DateFormat('dd/MM/yyyy').format(DateTime(2023, 3, 3))}', //
          'Comments: Professional, very enthusiastic rescuer', //
          4, // Example rating
        ),
        const SizedBox(height: 12),
        _buildReviewCard(
          context, // Truyền context vào đây
          'Res Trip on ${DateFormat('dd/MM/yyyy').format(DateTime(2023, 3, 3))}', //
          'Comments: Professional, very enthusiastic rescuer', //
          5, // Example rating
        ),
        const SizedBox(height: 12),
        _buildReviewCard(
          context, // Truyền context vào đây
          'Res Trip on ${DateFormat('dd/MM/yyyy').format(DateTime(2023, 3, 3))}', //
          'Comments: Professional, very enthusiastic rescuer', //
          3, // Example rating
        ),
      ],
    );
  }

  // Cần có BuildContext làm tham số đầu tiên
  Widget _buildReviewCard(
    BuildContext context,
    String title,
    String comment,
    int rating,
  ) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.black12.withOpacity(0.05),
            blurRadius: 8,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                title,
                style: const TextStyle(
                  fontWeight: FontWeight.w600,
                  fontSize: 15,
                  color: Colors.black87,
                ),
              ),
              ElevatedButton(
                onPressed: () {
                  // Gọi hàm để hiển thị Dialog khi nút Invoice được nhấn
                  // Sử dụng context được truyền vào
                  _showInvoiceDialog(context, title);
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.red,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(
                    horizontal: 12,
                    vertical: 4,
                  ),
                  minimumSize: Size.zero, // Remove default minimum size
                  tapTargetSize:
                      MaterialTapTargetSize.shrinkWrap, // Shrink tap area
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                  elevation: 0,
                ),
                child: const Text('Invoice', style: TextStyle(fontSize: 12)),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            comment,
            style: const TextStyle(fontSize: 13, color: Colors.grey),
          ),
          const SizedBox(height: 8),
          Row(
            children: List.generate(5, (index) {
              return Icon(
                index < rating ? Icons.star : Icons.star_border,
                color: Colors.amber,
                size: 20,
              );
            }),
          ),
        ],
      ),
    );
  }
}
