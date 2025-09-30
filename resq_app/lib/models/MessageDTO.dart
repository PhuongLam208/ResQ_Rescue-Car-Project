class MessageDTO {
  final int messageId;
  final String content;
  final String senderName;
  final String senderRole;
  final int senderId;
  final DateTime createdAt;

  MessageDTO({
    required this.messageId,
    required this.content,
    required this.senderName,
    required this.senderRole,
    required this.senderId,
    required this.createdAt,
  });

  factory MessageDTO.fromJson(Map<String, dynamic> json) {
    return MessageDTO(
      messageId: json['messageId'],
      content: json['content'],
      senderName: json['senderName'],
      senderRole: json['senderRole'],
      senderId: json['senderId'], // cần backend trả về
      createdAt: DateTime.parse(json['createdAt']),
    );
  }
}
