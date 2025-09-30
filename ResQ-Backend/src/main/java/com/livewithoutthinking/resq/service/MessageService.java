package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.dto.ConversationDTO;
import com.livewithoutthinking.resq.dto.MessageDTO;
import com.livewithoutthinking.resq.entity.*;
import com.livewithoutthinking.resq.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final StaffConversationRepository staffConversationRepository;

    public List<Message> findByConversation(int conversationId) {
        return messageRepository.findByConversation(conversationId);
    }

    public int findBestStaffReturnId() {
        List<Staff> availableStaff = staffRepository.findByOnShiftTrueAndIsOnlineTrue();

        if (availableStaff.isEmpty()) {
            throw new RuntimeException("No staff available");
        }

        // Tìm min chat count
        int minCount = availableStaff.stream()
                .mapToInt(Staff::getOnShiftChatCount)
                .min()
                .orElse(Integer.MAX_VALUE);

        // Lọc những staff có cùng min count
        List<Staff> minStaffs = availableStaff.stream()
                .filter(s -> s.getOnShiftChatCount() == minCount)
                .toList();

        // Chọn ngẫu nhiên 1 người trong số đó
        Staff selected = minStaffs.get(new Random().nextInt(minStaffs.size()));
        return selected.getUser().getUserId();
    }

    /**
     * Gửi 1 tin nhắn
     */
    public Message sendMessage(Integer customerId, Integer staffUserId, Integer senderId, String content, Integer conversationId) {

        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("The message content must not be empty.");
        }
        if (content.length() > 1000) {
            throw new RuntimeException("Your message is too long. Maximum allowed is 500 characters.");
        }
        Conversation conversation;

        if (conversationId != null) {
            conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
        } else {
            // Tìm hoặc tạo mới Conversation
            Optional<Staff> currentStaff = staffRepository.findByUser_UserId(staffUserId);

            if (currentStaff.isEmpty()) {
                throw new RuntimeException("Staff not found");
            }

            Staff staff;

            if (!currentStaff.get().isOnShift()) {
                Optional<Staff> newStaff = staffRepository.findByUser_UserId(findBestStaffReturnId());
                if (newStaff.isPresent()) {
                    staff = newStaff.get();
                } else {
                    throw new RuntimeException("No available staff found");
                }
            } else {
                staff = currentStaff.get();
            }

            conversation = staffConversationRepository
                    .findByStaff_StaffIdAndConversation_Sender_UserId(staff.getStaffId(), customerId)
                    .map(StaffConversation::getConversation)
                    .orElseGet(() -> {
                        Conversation newConv = new Conversation();
                        newConv.setSender(new User(customerId));
                        newConv.setCreatedAt(new Date());
                        newConv.setUpdatedAt(new Date());
                        newConv.setSubject("Khách hàng gửi yêu cầu hỗ trợ");
                        newConv.setUserType("user");
                        Conversation saved = conversationRepository.save(newConv);

                        StaffConversation sc = new StaffConversation();
                        sc.setStaff(staff);
                        sc.setConversation(saved);
                        sc.setAssignedAt(new Date());
                        staffConversationRepository.save(sc);

                        return saved;
                    });
        }

        // Tạo tin nhắn
        Message msg = new Message();
        msg.setConversation(conversation);
        msg.setContent(content);
        msg.setStatus(Message.Status.SENT);
        msg.setCreatedAt(new Date());
        msg.setUpdatedAt(new Date());

        // Phân biệt ai là người gửi (user hay staff)
        if (senderId.equals(customerId)) {
            User senderUser = userRepository.findById(senderId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            msg.setSender(senderUser);
            // Không cần setStaff
        } else if (senderId.equals(staffUserId)) {
            // Giả sử staff vẫn gửi thông qua User entity
            Optional<Staff> senderStaff = staffRepository.findByUser_UserId(senderId);
            if (senderStaff.isEmpty()) throw new RuntimeException("Staff not found");
            msg.setSender(senderStaff.get().getUser()); // Gán qua User
        }
        else {
            throw new RuntimeException("Sender ID không hợp lệ với customerId hoặc staffUserId");
        }

        return messageRepository.save(msg);
    }

    /** Tạo cuộc trò chuyện mới bẳng userId */
    public ConversationDTO createConversation(Integer senderId, Integer recipientId, String subject) {
        if (senderId.equals(recipientId)) {
            throw new RuntimeException("Cannot create a conversation with yourself.");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found."));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found."));

        // Check for existing open conversation
        List<Conversation> senderConvs = conversationRepository.findBySender_UserId(senderId)
                .stream()
                .filter(conv -> Boolean.FALSE.equals(conv.getIsClosed()))
                .toList();

        for (Conversation conv : senderConvs) {
            List<StaffConversation> scs = staffConversationRepository
                    .findByConversation_ConversationId(conv.getConversationId());
            boolean hasRecipient = scs.stream()
                    .anyMatch(sc -> sc.getStaff() != null
                            && sc.getStaff().getUser().getUserId().equals(recipientId));
            if (hasRecipient) {
                throw new RuntimeException("An open conversation already exists between the two users.");
            }
        }

        // Create new conversation
        Conversation conversation = new Conversation();
        conversation.setSender(sender);
        conversation.setSubject(subject != null ? subject : "New Conversation");
        conversation.setCreatedAt(new Date());
        conversation.setUpdatedAt(new Date());
        conversation.setIsClosed(false);
        Conversation savedConv = conversationRepository.save(conversation);

        // Link recipient (if staff)
        Optional<Staff> recipientStaff = staffRepository.findByUser_UserId(recipientId);
        if (recipientStaff.isPresent()) {
            StaffConversation sc = new StaffConversation();
            sc.setConversation(savedConv);
            sc.setStaff(recipientStaff.get());
            sc.setAssignedAt(new Date());
            staffConversationRepository.save(sc);
        }

        // Return as DTO
        return getConversationDTOById(savedConv.getConversationId());
    }

    /**
     * Lấy tất cả tin nhắn trong cuộc trò chuyện
     */
    public List<MessageDTO> getMessages(Integer conversationId) {
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (conversationOpt.isEmpty()) {
            throw new RuntimeException("Conversation not found!");
        }

        List<Message> messages = messageRepository.findByConversationOrderByCreatedAtAsc(conversationOpt.get());

        return messages.stream().map(msg -> {
            MessageDTO dto = new MessageDTO();
            dto.setMessageId(msg.getMessageId());

            dto.setSenderId(
                    msg.getSender() != null ? msg.getSender().getUserId() : null
            );

            dto.setSenderName(
                    msg.getSender() != null ? msg.getSender().getFullName() : "Unknown"
            );

            dto.setSenderRole(
                    msg.getSender() != null && msg.getSender().getRole() != null
                            ? msg.getSender().getRole().getRoleName().toLowerCase() // "user", "staff", v.v.
                            : "unknown"
            );

            dto.setContent(msg.getContent());
            dto.setCreatedAt(msg.getCreatedAt());
            return dto;
        }).toList();
    }



    /**
     * Đánh dấu tất cả tin nhắn chưa đọc thành đã đọc
     */
    public void markMessagesAsRead(Integer conversationId, Integer readerId) {
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (conversationOpt.isEmpty()) {
            throw new RuntimeException("Conversation not found!");
        }

        Conversation conversation = conversationOpt.get();
        List<Message> unreadMessages = messageRepository.findByConversationAndStatus(conversation, Message.Status.SENT);

        for (Message message : unreadMessages) {
            if (!message.getSender().getUserId().equals(readerId)) {
                message.setStatus(Message.Status.READ);
                message.setUpdatedAt(new Date());
            }
        }

        messageRepository.saveAll(unreadMessages);
    }

    // MessageService.java
    public List<ConversationDTO> getAllConversationsDTOByUserId(Integer userId) {
        List<Conversation> asSender = conversationRepository.findBySender_UserId(userId);

        // Tìm tất cả các conversation mà staff này tham gia (qua bảng trung gian)
        List<StaffConversation> staffConvs = staffConversationRepository.findByStaff_User_UserId(userId);
        List<Conversation> asStaff = staffConvs.stream()
                .map(StaffConversation::getConversation)
                .toList();

        asSender.addAll(asStaff);

        return asSender.stream().map(conv -> {
            ConversationDTO dto = new ConversationDTO();
            dto.setConversationId(conv.getConversationId());
            dto.setSubject(conv.getSubject());
            dto.setContactType(conv.getContactType() != null ? conv.getContactType().getName() : null);
            dto.setSenderId(conv.getSender() != null ? conv.getSender().getUserId() : null);
            dto.setIsClosed(conv.getIsClosed());
            dto.setUpdatedAt(conv.getUpdatedAt());


            // Join tên đối tác hiển thị (ví dụ luôn là sender)
            if (conv.getSender() != null) {
                User sender = conv.getSender();
                User senderDb = userRepository.findById(sender.getUserId()).orElse(null);
                if (senderDb != null) {
                    dto.setPartnerName(senderDb.getFullName());
                    dto.setPartnerAvatar("uploads/" + senderDb.getAvatar());
                    dto.setPartnerOnline(senderDb.getIsOnline());
                    dto.setUserType(conv.getUserType());
                } else {
                    dto.setPartnerName("Không xác định");
                    dto.setPartnerAvatar(null);
                }
            } else {
                dto.setPartnerName("Không xác định");
                dto.setPartnerAvatar(null);
            }

            return dto;
        }).toList();
    }


    public Conversation getConversationById(Integer conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }

    public Conversation saveConversation(Conversation conv) {
        conv.setUpdatedAt(new Date());
        return conversationRepository.save(conv);
    }

    public ConversationDTO getConversationDTOById(Integer conversationId) {
        Conversation conv = getConversationById(conversationId);

        ConversationDTO dto = new ConversationDTO();
        dto.setConversationId(conv.getConversationId());
        dto.setSubject(conv.getSubject());
        dto.setContactType(conv.getContactType() != null ? conv.getContactType().getName() : null);
        dto.setSenderId(conv.getSender() != null ? conv.getSender().getUserId() : null);
        dto.setIsClosed(conv.getIsClosed());
        dto.setUpdatedAt(conv.getUpdatedAt());

        if (conv.getSender() != null) {
            User sender = conv.getSender();
            User senderDb = userRepository.findById(sender.getUserId()).orElse(null);
            if (senderDb != null) {
                dto.setPartnerName(senderDb.getFullName());
                dto.setPartnerAvatar("uploads/" + senderDb.getAvatar());
                dto.setPartnerOnline(senderDb.getIsOnline());
                dto.setUserType(conv.getUserType());
            } else {
                dto.setPartnerName("Không xác định");
                dto.setPartnerAvatar(null);
            }
        } else {
            dto.setPartnerName("Không xác định");
            dto.setPartnerAvatar(null);
        }

        return dto;
    }

    // ✅ Hàm hỗ trợ lấy User từ userId hoặc từ partner.user
    private User getSenderUserByCustomerId(Integer customerId) {
        return userRepository.findById(customerId).orElseGet(() -> {
            // Nếu không có User, thử tìm qua Partner
            return userRepository.findById(customerId)
                    .flatMap(user -> Optional.ofNullable(user.getPartner()))
                    .map(Partner::getUser)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user hoặc partner tương ứng"));
        });
    }

    public Integer findStaffIdByConversationId(Integer conversationId) {
        List<StaffConversation> list = staffConversationRepository.findByConversation_ConversationId(conversationId);
        if (list.isEmpty()) {
            throw new RuntimeException("Không tìm thấy nhân viên cho conversationId = " + conversationId);
        }

        // Giả định nhân viên được gán mới nhất là người xử lý chính
        StaffConversation latest = list.stream()
                .max((a, b) -> a.getAssignedAt().compareTo(b.getAssignedAt()))
                .orElseThrow(() -> new RuntimeException("Không thể xác định nhân viên được gán mới nhất"));

        return latest.getStaff().getStaffId();
    }

    public List<StaffConversation> getStaffConversationsByConversationId(Integer conversationId) {
        List<StaffConversation> staffList = staffConversationRepository.findByConversation_ConversationId(conversationId);
        if (staffList.isEmpty()) {
            throw new RuntimeException("Không tìm thấy nhân viên nào cho conversationId = " + conversationId);
        }
        return staffList;
    }

    public Message sendMessageForceNewConversation(
            Integer customerId,
            Integer staffUserId,
            Integer senderId,
            String content
    ) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Nội dung tin nhắn không được để trống.");
        }

        if (content.length() > 1000) {
            throw new RuntimeException("Tin nhắn quá dài. Tối đa cho phép là 1000 ký tự.");
        }

        // Tạo conversation mới
        Optional<Staff> staff = staffRepository.findByUser_UserId(staffUserId);
        if (staff.isEmpty()) throw new RuntimeException("Không tìm thấy nhân viên.");

        staff.get().setOnShiftChatCount(
                staff.get().getOnShiftChatCount() == null ? 1 : staff.get().getOnShiftChatCount() + 1
        );
        staffRepository.save(staff.get());


        Conversation newConv = new Conversation();
        newConv.setSender(new User(customerId));
        newConv.setCreatedAt(new Date());
        newConv.setUpdatedAt(new Date());
        newConv.setSubject("Khách hàng gửi yêu cầu hỗ trợ");
        newConv.setIsClosed(false);

        Conversation savedConv = conversationRepository.save(newConv);

        // Gán staff vào conversation
        StaffConversation sc = new StaffConversation();
        sc.setStaff(staff.get());
        sc.setConversation(savedConv);
        sc.setAssignedAt(new Date());
        staffConversationRepository.save(sc);

        // Tạo tin nhắn đầu tiên
        Message msg = new Message();
        msg.setConversation(savedConv);
        msg.setContent(content);
        msg.setStatus(Message.Status.SENT);
        msg.setCreatedAt(new Date());
        msg.setUpdatedAt(new Date());

        // Phân biệt người gửi (user hoặc staff)
        if (senderId.equals(customerId)) {
            msg.setSender(getSenderUserByCustomerId(senderId));
        } else if (senderId.equals(staffUserId)) {
            Optional<Staff> senderStaff = staffRepository.findByUser_UserId(senderId);
            if (senderStaff.isEmpty()) throw new RuntimeException("Không tìm thấy nhân viên gửi.");
            msg.setSender(senderStaff.get().getUser());
        } else {
            throw new RuntimeException("Sender ID không hợp lệ.");
        }

        return messageRepository.save(msg);
    }

}
