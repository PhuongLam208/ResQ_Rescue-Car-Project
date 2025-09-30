import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:resq_app/config/app_config.dart';
import 'package:resq_app/widgets/common_app_bar.dart';

class NotificationScreen extends StatefulWidget {
  final int userId;

  const NotificationScreen({super.key, required this.userId});

  @override
  State<NotificationScreen> createState() => _NotificationScreenState();
}

class _NotificationScreenState extends State<NotificationScreen> {
  List<dynamic> notifications = [];
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    fetchNotifications();
  }

  Future<void> fetchNotifications() async {
    final response = await http.get(
      Uri.parse('$baseUrl/api/notifications/user/${widget.userId}'),
    );

    if (response.statusCode == 200) {
      setState(() {
        notifications = jsonDecode(response.body);
        isLoading = false;
      });
    } else {
      setState(() {
        notifications = jsonDecode(response.body);
        debugPrint("📥 Received data:");
        for (var n in notifications) {
          debugPrint(n.toString());
        }
        isLoading = false;
      });

      throw Exception("Failed to load notifications");
    }
  }

  Future<void> markAsRead(int notificationId) async {
    final url = Uri.parse(
      '$baseUrl/api/notifications/mark-as-read/$notificationId',
    );
    final response = await http.put(url);

    if (response.statusCode != 200) {
      debugPrint("⚠️ Failed to mark as read");
    }
  }

  Widget buildNotificationItem(dynamic item) {
    IconData icon =
        item['notiType'] == 'message' ? Icons.mail : Icons.local_offer;
    Color iconColor =
        item['notiType'] == 'message' ? Colors.green : Colors.orange;

    return ListTile(
      onTap: () {
        debugPrint(" Notification tapped, ID = ${item['noId']}");
        Future.delayed(Duration.zero, () {
          showDialog(
            context: context,
            builder: (BuildContext dialogContext) {
              return AlertDialog(
                title: Text(item['title'] ?? 'Notification'),
                content: Text(item['message'] ?? ''),
                actions: [
                  TextButton(
                    onPressed: () => Navigator.of(dialogContext).pop(),
                    child: const Text('Close'),
                  ),
                ],
              );
            },
          );
        });

        // Mark as read if unread
        if (item['isRead'] == false) {
          markAsRead(item['noId']);
          setState(() {
            item['isRead'] = true;
          });
        }
      },
      leading: CircleAvatar(
        backgroundColor: iconColor.withOpacity(0.15),
        child: Icon(icon, color: iconColor),
      ),
      title: Text(
        item['title'] ?? 'Notification',
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
        style: const TextStyle(fontWeight: FontWeight.bold),
      ),
      subtitle: Text(
        item['message'] ?? '',
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
      ),
      trailing:
          item['isRead'] == false
              ? const Icon(Icons.circle, size: 10, color: Color(0xFFBB0000))
              : null,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const CommonAppBar(title: 'Notifications'),
      body:
          isLoading
              ? const Center(child: CircularProgressIndicator())
              : notifications.isEmpty
              ? const Center(
                child: Text(
                  'No Notifications',
                  style: TextStyle(fontSize: 16, color: Colors.grey),
                ),
              )
              : ListView.separated(
                itemCount: notifications.length,
                separatorBuilder: (context, index) => const Divider(height: 1),
                itemBuilder: (context, index) {
                  return buildNotificationItem(notifications[index]);
                },
              ),
    );
  }
}
