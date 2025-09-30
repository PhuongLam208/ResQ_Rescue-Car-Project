import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:resq_app/models/MessageDTO.dart';
import 'package:resq_app/config/app_config.dart';
import 'package:resq_app/widgets/common_app_bar.dart';

class ChatScreen extends StatefulWidget {
  final int userId;
  final int staffUserId;
  final int conversationId;
  final String staffName;
  final bool isClosed;

  const ChatScreen({
    super.key,
    required this.userId,
    required this.staffUserId,
    required this.conversationId,
    required this.staffName,
    required this.isClosed,
  });

  @override
  State<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends State<ChatScreen> {
  List<MessageDTO> messages = [];
  bool isLoading = true;
  final TextEditingController _controller = TextEditingController();
  final ScrollController _scrollController = ScrollController();
  late bool isClosed;

  @override
  void initState() {
    super.initState();
    isClosed = widget.isClosed;
    fetchMessages();
  }

  Future<void> fetchMessages() async {
    final response = await http.get(
      Uri.parse('$baseUrl/api/messages/${widget.conversationId}'),
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = json.decode(response.body);
      setState(() {
        messages = data.map((json) => MessageDTO.fromJson(json)).toList();
        isLoading = false;
      });

      WidgetsBinding.instance.addPostFrameCallback((_) {
        _scrollController.jumpTo(_scrollController.position.maxScrollExtent);
      });
    } else {
      throw Exception("Failed to load messages");
    }
  }

  Future<void> closeConversation() async {
    final url = Uri.parse('$baseUrl/api/messages/conversation/${widget.conversationId}/close');
    final response = await http.post(url);

    if (response.statusCode == 200) {
      setState(() {
        isClosed = true;
      });

      await fetchMessages();

      showDialog(
        context: context,
        builder: (_) => AlertDialog(
          title: const Text("Conversation Closed"),
          content: const Text("The conversation has been successfully closed."),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text("OK"),
            )
          ],
        ),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Failed to close the conversation")),
      );
    }
  }

  Future<void> sendMessage() async {
    final text = _controller.text.trim();
    if (text.isEmpty) return;

    // Gọi API để kiểm tra trạng thái của conversation
    final checkUrl = Uri.parse(
        'http://10.0.2.2:9090/api/messages/conversation/${widget.conversationId}');
    final checkResponse = await http.get(checkUrl);

    if (checkResponse.statusCode == 200) {
      final data = json.decode(checkResponse.body);
      final bool conversationClosed = data['isClosed'] ?? false;

      if (conversationClosed) {
        setState(() {
          isClosed = true;
        });
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text("Cuộc trò chuyện đã kết thúc. Không thể gửi tin nhắn."), backgroundColor: Colors.red),
        );
        return;
      }
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Không kiểm tra được trạng thái hội thoại."), backgroundColor: Colors.red),
      );
      return;
    }

    // Nếu chưa đóng thì tiếp tục gửi tin nhắn
    final url = Uri.parse('http://10.0.2.2:9090/api/messages/send');
    final response = await http.post(
      url,
      body: {
        'customerId': widget.userId.toString(),
        'staffUserId': widget.staffUserId.toString(),
        'senderId': widget.userId.toString(),
        'content': text,
        'conversationId': widget.conversationId.toString()
      },
    );

    if (response.statusCode == 200) {
      _controller.clear();
      fetchMessages();
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Không gửi được tin nhắn"), backgroundColor: Colors.red),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const CommonAppBar(title: 'Chat Screen'),
      body: Column(
        children: [
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
            color: Colors.white,
            child: Row(
              children: [
                const CircleAvatar(
                  backgroundImage: AssetImage('assets/images/avatar.jpg'),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        widget.staffName,
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 16,
                          color: Colors.black,
                        ),
                      ),
                      const Text(
                        '● Active',
                        style: TextStyle(fontSize: 12, color: Colors.green),
                      ),
                    ],
                  ),
                ),
                if (!isClosed)
                  IconButton(
                    icon: const Icon(Icons.close, color: Color(0xFFBB0000)),
                    tooltip: "Close conversation",
                    onPressed: closeConversation,
                  )
              ],
            ),
          ),
          const Divider(height: 1),
          Expanded(
            child: isLoading
                ? const Center(child: CircularProgressIndicator())
                : ListView.builder(
                    controller: _scrollController,
                    padding: const EdgeInsets.all(12),
                    itemCount: messages.length,
                    itemBuilder: (context, index) {
                      final msg = messages[index];
                      final isMe = msg.senderId == widget.userId;

                      return Align(
                        alignment: isMe ? Alignment.centerRight : Alignment.centerLeft,
                        child: Container(
                          margin: const EdgeInsets.symmetric(vertical: 6),
                          padding: const EdgeInsets.all(12),
                          constraints: BoxConstraints(
                            maxWidth: MediaQuery.of(context).size.width * 0.7,
                          ),
                          decoration: BoxDecoration(
                            color: isMe ? const Color(0xFF013171).withOpacity(0.1) : Colors.grey.shade200,
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: Column(
                            crossAxisAlignment: isMe ? CrossAxisAlignment.end : CrossAxisAlignment.start,
                            children: [
                              Text(
                                msg.senderName,
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  fontSize: 12,
                                  color: Colors.grey[700],
                                ),
                              ),
                              const SizedBox(height: 4),
                              Text(msg.content),
                              const SizedBox(height: 4),
                              Text(
                                "${msg.createdAt.hour}:${msg.createdAt.minute.toString().padLeft(2, '0')}",
                                style: const TextStyle(fontSize: 10, color: Colors.grey),
                              )
                            ],
                          ),
                        ),
                      );
                    },
                  ),
          ),
          const Divider(height: 1),
          isClosed
              ? Container(
                  padding: const EdgeInsets.all(16),
                  color: Colors.grey.shade200,
                  child: const Text(
                    "This conversation is closed. You cannot send more messages.",
                    style: TextStyle(color: Colors.grey),
                  ),
                )
              : Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 6),
                  color: Colors.white,
                  child: Row(
                    children: [
                      Expanded(
                        child: TextField(
                          controller: _controller,
                          decoration: const InputDecoration(
                            hintText: "Type your message...",
                            border: InputBorder.none,
                          ),
                        ),
                      ),
                      IconButton(
                        icon: const Icon(Icons.send, color: Color(0xFF013171)),
                        onPressed: sendMessage,
                      ),
                    ],
                  ),
                ),
        ],
      ),
    );
  }
}
