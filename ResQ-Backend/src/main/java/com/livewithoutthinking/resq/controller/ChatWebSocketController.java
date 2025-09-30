package com.livewithoutthinking.resq.controller;

import com.livewithoutthinking.resq.dto.MessageDTO;
import com.livewithoutthinking.resq.entity.Conversation;
import com.livewithoutthinking.resq.entity.Message;
import com.livewithoutthinking.resq.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate, MessageService messageService) {
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(MessageDTO message) {

        Conversation conversation = messageService.getConversationById(message.getConversationId());
        if (conversation.getIsClosed()) {
            // Không cho gửi, có thể thông báo lỗi
            return;
        }
        // Lưu tin nhắn vào DB
        Message saved = messageService.sendMessage(
                message.getSenderId(),
                message.getRecipientId(),
                message.getSenderId(),
                message.getContent(),
                message.getConversationId()
        );

        // Broadcast tin nhắn tới cả hai người dùng (có thể tuỳ chỉnh theo phòng)
        MessageDTO response = new MessageDTO();
        response.setMessageId(saved.getMessageId());
        response.setSenderId(saved.getSender().getUserId());
        response.setContent(saved.getContent());
        response.setCreatedAt(saved.getCreatedAt());

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + saved.getConversation().getConversationId(),
                response
        );
    }
}

