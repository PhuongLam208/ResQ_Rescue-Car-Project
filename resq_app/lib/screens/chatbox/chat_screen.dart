import 'dart:convert';
import 'dart:async';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:resq_app/config/app_config.dart';
import 'package:resq_app/widgets/common_app_bar.dart';

class ChatBotScreen extends StatefulWidget {
  final int userId;
  const ChatBotScreen({super.key, required this.userId});

  @override
  State<ChatBotScreen> createState() => _ChatBotScreenState();
}

class _ChatBotScreenState extends State<ChatBotScreen> {
  List<Map<String, dynamic>> messages = [];
  List<String> suggestions = [];
  bool isFinalStep = false;
  late int currentUserId;
  int? currentConversationId;
  int? staffUserId;
  bool isChatWithBot = true;
  Timer? messagePollingTimer;

  final ScrollController _scrollController = ScrollController();
  final TextEditingController _controller = TextEditingController();

  @override
  void initState() {
    super.initState();
    currentUserId = widget.userId;
    fetchChatBotStep("start");
  }

  Future<void> loadConversationMessages(int conversationId) async {
    final res = await http.get(
      Uri.parse('$baseUrl/api/messages/$conversationId'),
    );
    if (res.statusCode == 200) {
      final List<dynamic> data = jsonDecode(res.body);
      final loaded =
          data.map<Map<String, dynamic>>((item) {
            return {
              'text': item['content'],
              'isBot': item['senderId'] != currentUserId,
            };
          }).toList();

      setState(() {
        messages = loaded;
      });
      scrollToBottom();
    }
  }

  Future<void> loadConversationFromUserId() async {
    final res = await http.get(
      Uri.parse('$baseUrl/api/messages/conversation/user/all/$currentUserId'),
    );
    if (res.statusCode == 200) {
      final List<dynamic> data = jsonDecode(res.body);
      final filtered = data.firstWhere(
        (item) => item['isClosed'] == false || item['isClosed'] == null,
        orElse: () => null,
      );

      if (filtered == null) {
        setState(() {
          messages.add({
            'text':
                'There is currently no open conversation with support staff.',
            'isBot': true,
          });
        });
        return;
      }

      currentConversationId = filtered['conversationId'];
      final resStaff = await http.get(
        Uri.parse(
          '$baseUrl/api/messages/conversation/$currentConversationId/staff-id',
        ),
      );
      if (resStaff.statusCode == 200) {
        staffUserId = jsonDecode(resStaff.body);
      }
      await loadConversationMessages(currentConversationId!);
    } else {
      setState(() {
        messages.add({
          'text': 'Unable to fetch conversation list.',
          'isBot': true,
        });
      });
    }
  }

  Future<void> fetchChatBotStep(String userMessage) async {
    final staffIdRes = await http.get(
      Uri.parse('$baseUrl/api/messages/get-staffId'),
    );
    staffUserId =
        staffIdRes.statusCode == 200 ? int.parse(staffIdRes.body) : null;

    Map<String, dynamic> requestBody = {
      'message': userMessage,
      'userId': widget.userId.toString(),
      'staffUserId': staffUserId?.toString() ?? '1',
    };

    final res = await http.post(
      Uri.parse('$baseUrl/api/chatbot/next'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(requestBody),
    );

    print("➡️ [API] POST /chatbot/next");
    print("📝 Sent message: $userMessage");
    print("🔽 Response: ${res.statusCode}");
    print("📦 Body: ${res.body}");

    final data = jsonDecode(res.body);
    final botMessage = data['message'] ?? '';
    final bool finalStepFromApi = data['finalStep'] ?? false;

    setState(() {
      messages.add({'text': userMessage, 'isBot': false});
      if (botMessage.isNotEmpty)
        messages.add({'text': botMessage, 'isBot': true});
      suggestions = List<String>.from(data['suggestions'] ?? []);
      isFinalStep = finalStepFromApi;
    });

    if (isFinalStep) {
      await Future.delayed(const Duration(milliseconds: 600));
      await loadConversationFromUserId();
      setState(() => isChatWithBot = false);
      messagePollingTimer = Timer.periodic(const Duration(seconds: 3), (_) {
        if (currentConversationId != null && !isChatWithBot) {
          loadConversationMessages(currentConversationId!);
        }
      });
    }
  }

  Future<void> sendRealMessage(String content) async {
    if (currentConversationId == null || staffUserId == null) return;
    await http.post(
      Uri.parse('$baseUrl/api/messages/send'),
      headers: {'Content-Type': 'application/x-www-form-urlencoded'},
      body: {
        'customerId': currentUserId.toString(),
        'staffUserId': staffUserId.toString(),
        'senderId': currentUserId.toString(),
        'content': content,
        'conversationId': currentConversationId.toString(),
      },
    );
  }

  void sendMessage() {
    final text = _controller.text.trim();
    if (text.isEmpty) return;

    if (!isChatWithBot) {
      sendRealMessage(
        text,
      ).then((_) => loadConversationMessages(currentConversationId!));
    } else {
      fetchChatBotStep(text);
    }
    _controller.clear();
  }

  void scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  @override
  void dispose() {
    messagePollingTimer?.cancel();
    _controller.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const CommonAppBar(title: 'Chat'),
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
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: const [
                    Text(
                      'Support Staff',
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 16,
                        color: Colors.black,
                      ),
                    ),
                    Text(
                      '● Active',
                      style: TextStyle(fontSize: 12, color: Colors.green),
                    ),
                  ],
                ),
              ],
            ),
          ),
          Expanded(
            child: ListView.builder(
              controller: _scrollController,
              padding: const EdgeInsets.all(16),
              itemCount: messages.length,
              itemBuilder: (context, index) {
                final msg = messages[index];
                final isBot = msg['isBot'] as bool;
                return Align(
                  alignment:
                      isBot ? Alignment.centerLeft : Alignment.centerRight,
                  child: Container(
                    margin: const EdgeInsets.symmetric(vertical: 4),
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: isBot ? Colors.grey[200] : const Color(0xFF013171),
                      borderRadius: BorderRadius.circular(16),
                    ),
                    child: Text(
                      msg['text'],
                      style: TextStyle(
                        color: isBot ? Colors.black : Colors.white,
                      ),
                    ),
                  ),
                );
              },
            ),
          ),
          if (!isFinalStep && suggestions.isNotEmpty)
            Wrap(
              spacing: 8,
              runSpacing: 4,
              children:
                  suggestions.map((sug) {
                    return ElevatedButton(
                      onPressed: () => fetchChatBotStep(sug),
                      child: Text(sug),
                    );
                  }).toList(),
            ),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
            decoration: const BoxDecoration(
              border: Border(top: BorderSide(color: Color(0xFF013171))),
              color: Colors.white,
            ),
            child: Row(
              children: [
                IconButton(
                  icon: const Icon(Icons.add, color: Color(0xFF013171)),
                  onPressed: () {},
                ),
                Expanded(
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12),
                    decoration: BoxDecoration(
                      border: Border.all(color: Color(0xFF013171)),
                      borderRadius: BorderRadius.circular(24),
                    ),
                    child: Row(
                      children: [
                        Expanded(
                          child: TextField(
                            controller: _controller,
                            decoration: const InputDecoration(
                              hintText: "Type a message...",
                              border: InputBorder.none,
                            ),
                          ),
                        ),
                        const Icon(Icons.emoji_emotions, color: Colors.amber),
                      ],
                    ),
                  ),
                ),
                const SizedBox(width: 8),
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
