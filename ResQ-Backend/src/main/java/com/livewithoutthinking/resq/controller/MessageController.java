package com.livewithoutthinking.resq.controller;

import com.livewithoutthinking.resq.dto.ConversationDTO;
import com.livewithoutthinking.resq.dto.MessageDTO;
import com.livewithoutthinking.resq.entity.Conversation;
import com.livewithoutthinking.resq.entity.Message;
import com.livewithoutthinking.resq.entity.Staff;
import com.livewithoutthinking.resq.entity.StaffConversation;
import com.livewithoutthinking.resq.service.MessageService;
import com.livewithoutthinking.resq.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    @Autowired
    private StaffService staffService;

    @GetMapping("/get-staffId")
    public ResponseEntity<?> getBestAvailableStaffId() {
        Optional<Staff> selected = staffService.findByUser_UserId(messageService.findBestStaffReturnId());
        if (selected.isEmpty()) {
            return ResponseEntity.status(404).body("No Staff Available Now");
        }
        return ResponseEntity.ok(selected.get().getUser().getUserId());
    }


    /**
     * API gửi 1 tin nhắn
     */
    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(
            @RequestParam Integer customerId,
            @RequestParam Integer staffUserId,
            @RequestParam Integer senderId,
            @RequestParam String content,
            @RequestParam(required = false) Integer conversationId
    ) {
        Message message = messageService.sendMessage(customerId, staffUserId, senderId, content, conversationId);
        return ResponseEntity.status(200).body(message);
    }

    /**
     * API lấy tất cả tin nhắn trong 1 cuộc trò chuyện
     */
    @GetMapping("/{conversationId}")
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable Integer conversationId) {
        return ResponseEntity.status(200).body(messageService.getMessages(conversationId));
    }

    /**
     * API đánh dấu tất cả tin nhắn trong cuộc trò chuyện là đã đọc
     */
    @PostMapping("/{conversationId}/mark-as-read")
    public ResponseEntity<String> markMessagesAsRead(
            @PathVariable Integer conversationId,
            @RequestParam Integer readerId
    ) {
        messageService.markMessagesAsRead(conversationId, readerId);
        return ResponseEntity.status(200).body("All messages marked as READ.");
    }

    // MessageController.java
    @GetMapping("/conversation/user/{userId}")
    public ResponseEntity<ConversationDTO> getConversationByUserId(@PathVariable Integer userId) {
        List<ConversationDTO> conversations = messageService.getAllConversationsDTOByUserId(userId);
        if (conversations.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(conversations.get(0));
    }

    @GetMapping("/conversation/user/all/{userId}")
    public ResponseEntity<List<ConversationDTO>> getAllConversations(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(messageService.getAllConversationsDTOByUserId(userId));
    }

    @PostMapping("/conversation/{conversationId}/close")
    public ResponseEntity<String> closeConversation(@PathVariable Integer conversationId) {
        Conversation conv = messageService.getConversationById(conversationId);
        if (conv == null) {
            return ResponseEntity.status(404).body("Conversation not found.");
        }
        if (Boolean.TRUE.equals(conv.getIsClosed())) {
            return ResponseEntity.status(400).body("Conversation is already closed.");
        }
        conv.setIsClosed(true);
        conv.setUpdatedAt(new Date());
        messageService.saveConversation(conv);
        return ResponseEntity.ok("Conversation closed.");
    }

    @GetMapping("/conversation/{id}")
    public ResponseEntity<ConversationDTO> getConversationById(@PathVariable Integer id) {
        return ResponseEntity.status(200).body(messageService.getConversationDTOById(id));
    }

    @PostMapping("/conversation/create")
    public ResponseEntity<ConversationDTO> createConversation(
            @RequestParam Integer senderId,
            @RequestParam Integer recipientId,
            @RequestParam String subject
    ) {
        ConversationDTO conv = messageService.createConversation(senderId, recipientId, subject);
        return ResponseEntity.ok(conv);
    }

    @GetMapping("/conversation/{conversationId}/staff-id")
    public ResponseEntity<Integer> getStaffId(@PathVariable Integer conversationId) {
        Integer staffId = messageService.findStaffIdByConversationId(conversationId);
        return ResponseEntity.ok(staffId);
    }

    @GetMapping("/conversation/{conversationId}/staff-info")
    public ResponseEntity<List<Map<String, Object>>> getAllStaffInfoByConversationId(@PathVariable Integer conversationId) {
        List<StaffConversation> scList = messageService.getStaffConversationsByConversationId(conversationId);

        List<Map<String, Object>> result = scList.stream().map(sc -> {
            Map<String, Object> item = new HashMap<>();
            item.put("staffId", sc.getStaff().getStaffId());
            item.put("staffName", sc.getStaff().getUser().getFullName());
            item.put("assignedAt", sc.getAssignedAt());
            return item;
        }).toList();

        return ResponseEntity.ok(result);
    }
    @PostMapping("/send-force-new")
    public Message sendForceNewMessage(
            @RequestParam Integer customerId,
            @RequestParam Integer staffUserId,
            @RequestParam Integer senderId,
            @RequestParam String content
    ) {
        return messageService.sendMessageForceNewConversation(customerId, staffUserId, senderId, content);
    }


}
