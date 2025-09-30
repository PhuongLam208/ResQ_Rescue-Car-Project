import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:resq_app/screens/chatbox/chat_screen.dart';
import 'package:resq_app/config/app_config.dart';
import 'package:resq_app/models/ConversationDTO.dart';
import 'package:resq_app/models/StaffInfo.dart';
import 'package:resq_app/widgets/common_app_bar.dart';
import 'ChatScreen.dart';

class MessagesScreen extends StatefulWidget {
  final int userId;

  const MessagesScreen({super.key, required this.userId});

  @override
  State<MessagesScreen> createState() => _MessagesScreenState();
}

class _MessagesScreenState extends State<MessagesScreen> {
  List<ConversationDTO> conversations = [];
  bool isLoading = true;
  late final int userId;
  final TextEditingController _searchController = TextEditingController();
  String searchTerm = "";

  Map<int, String> staffNames = {}; // key: conversationId, value: staffName
  // giả định user đang đăng nhập có ID là 48

  @override
  void initState() {
    super.initState();
    userId = widget.userId;
    fetchConversations();
  }

  Future<void> fetchConversations() async {
    final url = Uri.parse(
      '$baseUrl/api/messages/conversation/user/all/$userId',
    );
    final response = await http.get(url, headers: headers);

    if (response.statusCode == 200) {
      final List<dynamic> data = json.decode(response.body);

      final List<ConversationDTO> fetchedConversations =
          data.map((json) => ConversationDTO.fromJson(json)).toList();

      for (var conv in fetchedConversations) {
        print(
          "🧾 ConversationID: ${conv.conversationId} | isClosed: ${conv.isClosed}",
        );
        final staffRes = await http.get(
          Uri.parse(
            '$baseUrl/api/messages/conversation/${conv.conversationId}/staff-info',
          ),
          headers: headers,
        );

        if (staffRes.statusCode == 200) {
          final staffList = json.decode(staffRes.body);
          if (staffList is List && staffList.isNotEmpty) {
            final firstStaff = staffList[0]; // staff đầu tiên
            staffNames[conv.conversationId] = firstStaff['staffName'];
          }
        }
      }

      setState(() {
        conversations = fetchedConversations;
        isLoading = false;
      });
    } else {
      throw Exception('Failed to load conversations');
    }
  }

  Future<void> openChat(ConversationDTO conv) async {
    try {
      final res = await http.get(
        Uri.parse(
          '$baseUrl/api/messages/conversation/${conv.conversationId}/staff-info',
        ),
      );

      if (res.statusCode == 200) {
        final decoded = json.decode(res.body);

        if (decoded is List && decoded.isNotEmpty) {
          final firstStaffJson = decoded[0]; // 👈 lấy nhân viên đầu tiên
          final staffInfo = StaffInfo.fromJson(firstStaffJson);

          // ✅ Đánh dấu đã đọc tin nhắn
          await http.post(
            Uri.parse(
              '$baseUrl/api/messages/${conv.conversationId}/mark-as-read',
            ),
            body: {'readerId': userId.toString()},
          );

          Navigator.push(
            context,
            MaterialPageRoute(
              builder:
                  (_) => ChatScreen(
                    userId: userId,
                    staffUserId: staffInfo.staffId,
                    conversationId: conv.conversationId,
                    staffName: staffInfo.staffName,
                    isClosed: conv.isClosed,
                  ),
            ),
          );
        } else {
          throw Exception('No staff member found in the conversation');
        }
      } else {
        throw Exception('Error calling staff-info API');
      }
    } catch (e) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('❌ ${e.toString()}')));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: const CommonAppBar(title: 'Messages'),
      body:
          isLoading
              ? const Center(child: CircularProgressIndicator())
              : Column(
                children: [
                  Padding(
                    padding: const EdgeInsets.all(12),
                    child: Row(
                      children: [
                        Expanded(
                          child: TextField(
                            controller: _searchController,
                            decoration: InputDecoration(
                              hintText: 'Search by staff...',
                              contentPadding: const EdgeInsets.symmetric(
                                horizontal: 12,
                              ),
                              border: OutlineInputBorder(
                                borderRadius: BorderRadius.circular(12),
                              ),
                            ),
                            onChanged: (value) {
                              setState(() {
                                searchTerm = value.trim().toLowerCase();
                              });
                            },
                          ),
                        ),
                        const SizedBox(width: 8),
                        IconButton(
                          icon: const Icon(Icons.refresh),
                          onPressed: () {
                            _searchController.clear();
                            setState(() {
                              searchTerm = "";
                              isLoading = true;
                            });
                            fetchConversations();
                          },
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 12),
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Container(
                        margin: const EdgeInsets.only(left: 12, bottom: 16),
                        child: ElevatedButton.icon(
                          style: ElevatedButton.styleFrom(
                            backgroundColor: Color(0xFFBB0000),
                            padding: const EdgeInsets.symmetric(
                              horizontal: 20,
                              vertical: 12,
                            ),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(10),
                            ),
                          ),
                          icon: const Icon(Icons.warning, color: Colors.white),
                          label: const Text(
                            "Emergency Assistance",
                            style: TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          onPressed: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (_) => ChatBotScreen(userId: userId),
                              ),
                            );
                          },
                        ),
                      ),
                    ],
                  ),

                  Expanded(
                    child:
                        conversations.isEmpty
                            ? const Center(
                              child: Text(
                                '🗨️ No conversation found.',
                                style: TextStyle(
                                  fontSize: 16,
                                  color: Colors.grey,
                                ),
                              ),
                            )
                            : ListView.builder(
                              itemCount: conversations.length,
                              itemBuilder: (context, index) {
                                final conv = conversations[index];
                                final staffName =
                                    staffNames[conv.conversationId] ?? "";

                                if (searchTerm.isNotEmpty &&
                                    !staffName.toLowerCase().contains(
                                      searchTerm,
                                    )) {
                                  return const SizedBox.shrink();
                                }

                                return ListTile(
                                  leading: CircleAvatar(
                                    backgroundImage:
                                        conv.partnerAvatar.contains('null')
                                            ? const AssetImage(
                                                  'assets/images/logo.png',
                                                )
                                                as ImageProvider
                                            : NetworkImage(
                                              "http://10.0.2.2:9090/${conv.partnerAvatar}",
                                            ),
                                  ),
                                  title: Text(
                                    staffName.isEmpty
                                        ? "Đang tải tên nhân viên..."
                                        : staffName,
                                    style: const TextStyle(
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                  subtitle: Column(
                                    crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                    children: [
                                      Text(conv.subject),
                                      if (staffNames.containsKey(
                                        conv.conversationId,
                                      ))
                                        Text(
                                          "Nhân viên: $staffName",
                                          style: const TextStyle(
                                            fontSize: 12,
                                            fontStyle: FontStyle.italic,
                                          ),
                                        ),
                                    ],
                                  ),
                                  trailing:
                                      conv.isClosed
                                          ? const Icon(
                                            Icons.lock,
                                            color: Colors.grey,
                                          )
                                          : const Icon(
                                            Icons.arrow_forward_ios,
                                            size: 16,
                                          ),
                                  onTap: () => openChat(conv),
                                );
                              },
                            ),
                  ),
                ],
              ),
    );
  }
}
