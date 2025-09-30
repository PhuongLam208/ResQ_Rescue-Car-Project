import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:web_socket_channel/web_socket_channel.dart';
import 'package:resq_app/config/app_config.dart'; // Đảm bảo AppConfig được import đúng

class RescueRequestWebSocket extends StatefulWidget {
  final int partnerId;

  const RescueRequestWebSocket({super.key, required this.partnerId});

  @override
  State<RescueRequestWebSocket> createState() => _RescueRequestWebSocketState();
}

class _RescueRequestWebSocketState extends State<RescueRequestWebSocket> {
  late WebSocketChannel _channel;
  Map<String, dynamic>? currentRequest;

  // Sử dụng AppConfig cho các URL

  final String wsUrl =
      '${wsBaseUrl}/ws/rescue/${DateTime.now().millisecondsSinceEpoch}'; // Bạn có thể cân nhắc dùng widget.partnerId thay cho DateTime.now() nếu backend hỗ trợ

  @override
  void initState() {
    super.initState();
    _channel = WebSocketChannel.connect(Uri.parse(wsUrl));

    _channel.stream.listen(
      (message) {
        debugPrint("📩 WebSocket received: $message");
        try {
          final data = jsonDecode(message);
          if (data['type'] == 'RESCUE_REQUEST') {
            setState(() {
              currentRequest = data['payload'];
            });
          }
        } catch (e) {
          debugPrint("❌ Error parsing WebSocket data: $e");
        }
      },
      onError: (error) {
        debugPrint("❌ WebSocket error: $error");
      },
      onDone: () {
        debugPrint("❌ WebSocket closed");
      },
    );
  }

  @override
  void dispose() {
    _channel.sink.close();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    // Widget này sẽ hiển thị yêu cầu nếu có,
    // nhưng bạn có thể không cần hiển thị nó trực tiếp nếu bạn dùng AlertDialog như ở PartnerDashboardScreen
    return currentRequest != null
        ? Card(
          margin: const EdgeInsets.all(16),
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  "🆘 Rescue Request",
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                const SizedBox(height: 8),
                Text("Type: ${currentRequest!['rescueType']}"),
                Text("Time: ${currentRequest!['createdAt']}"),
                Text("Lat: ${currentRequest!['latitude']}"),
                Text("Lon: ${currentRequest!['longitude']}"),
                const SizedBox(height: 12),
                // Nút "Chấp nhận" và "Từ chối" đã được chuyển sang AlertDialog trong PartnerDashboardScreen
                // Nếu bạn muốn hiển thị ở đây, bạn sẽ cần truyền hàm callback từ cha xuống.
                // Hiện tại, tôi khuyến nghị xử lý qua AlertDialog trong PartnerDashboardScreen.
              ],
            ),
          ),
        )
        : const Center(child: Text("⏳ Đang chờ yêu cầu cứu hộ..."));
  }
}
