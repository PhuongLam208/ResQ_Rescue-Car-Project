class ConversationDTO {
  final int conversationId;
  final String subject;
  final String? contactType;
  final int senderId;
  final String partnerName;
  final String partnerAvatar;
  final bool isClosed;
  final String updatedAt;
  final String userType;

  ConversationDTO({
    required this.conversationId,
    required this.subject,
    this.contactType,
    required this.senderId,
    required this.partnerName,
    required this.partnerAvatar,
    required this.isClosed,
    required this.updatedAt,
    required this.userType,
  });

  factory ConversationDTO.fromJson(Map<String, dynamic> json) {
    return ConversationDTO(
      conversationId: json['conversationId'],
      subject: json['subject'] ?? '',
      contactType: json['contactType'],
      senderId: json['senderId'],
      partnerName: json['partnerName'] ?? '',
      partnerAvatar: json['partnerAvatar'] ?? '',
      isClosed: json['isClosed'] ?? false,
      updatedAt: json['updatedAt'] ?? '',
      userType: json['userType'] ?? '',
    );
  }
}
