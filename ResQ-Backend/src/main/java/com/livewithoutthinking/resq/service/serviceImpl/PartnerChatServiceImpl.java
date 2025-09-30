package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.dto.ConversationDTO;
import com.livewithoutthinking.resq.dto.MessageDTO;
import com.livewithoutthinking.resq.entity.*;
import com.livewithoutthinking.resq.repository.*;
import com.livewithoutthinking.resq.service.PartnerChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PartnerChatServiceImpl implements PartnerChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final StaffConversationRepository staffConversationRepository;

    @Override
    public MessageDTO sendPartnerMessage(Integer staffId, Integer partnerUserId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Nội dung tin nhắn không được để trống.");
        }

        User partnerUser = userRepository.findById(partnerUserId)
                .orElseThrow(() -> new RuntimeException("Partner user not found"));

        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        Conversation conversation = conversationRepository
                .findBySender_UserIdAndUserType(partnerUserId, "partner")
                .stream()
                .filter(c -> staffConversationRepository
                        .findByConversation_ConversationId(c.getConversationId())
                        .stream()
                        .anyMatch(sc -> sc.getStaff().getStaffId() == staffId))
                .findFirst()
                .orElseGet(() -> {
                    Conversation conv = new Conversation();
                    conv.setSender(partnerUser);
                    conv.setUserType("partner");
                    conv.setSubject("Đối tác liên hệ hỗ trợ");
                    conv.setCreatedAt(new Date());
                    conv.setUpdatedAt(new Date());

                    Conversation saved = conversationRepository.save(conv);

                    StaffConversation sc = new StaffConversation();
                    sc.setStaff(staff);
                    sc.setConversation(saved);
                    sc.setAssignedAt(new Date());
                    staffConversationRepository.save(sc);

                    return saved;
                });

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(partnerUser);
        message.setContent(content);
        message.setStatus(Message.Status.SENT);
        message.setCreatedAt(new Date());
        message.setUpdatedAt(new Date());

        Message saved = messageRepository.save(message);

        MessageDTO dto = new MessageDTO();
        dto.setMessageId(saved.getMessageId());
        dto.setSenderId(partnerUserId);
        dto.setSenderName(partnerUser.getFullName());
        dto.setSenderRole("partner");
        dto.setContent(content);
        dto.setCreatedAt(saved.getCreatedAt());
        return dto;
    }

    @Override
    public List<MessageDTO> getPartnerMessages(Integer conversationId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // ✅ Kiểm tra userType
        if (!"partner".equalsIgnoreCase(conv.getUserType())) {
            throw new RuntimeException("Không phải cuộc trò chuyện với partner.");
        }

        return messageRepository.findByConversationOrderByCreatedAtAsc(conv)
                .stream()
                .map(msg -> {
                    MessageDTO dto = new MessageDTO();
                    dto.setMessageId(msg.getMessageId());
                    dto.setSenderId(msg.getSender().getUserId());
                    dto.setSenderName(msg.getSender().getFullName());
                    dto.setSenderRole("partner");
                    dto.setContent(msg.getContent());
                    dto.setCreatedAt(msg.getCreatedAt());
                    return dto;
                })
                .toList();
    }


    @Override
    public ConversationDTO getConversationDTOById(Integer conversationId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        ConversationDTO dto = new ConversationDTO();
        dto.setConversationId(conv.getConversationId());
        dto.setSubject(conv.getSubject());
        dto.setSenderId(conv.getSender() != null ? conv.getSender().getUserId() : null);
        dto.setIsClosed(conv.getIsClosed());
        dto.setUpdatedAt(conv.getUpdatedAt());

        if (conv.getSender() != null) {
            User senderDb = userRepository.findById(conv.getSender().getUserId()).orElse(null);
            if (senderDb != null) {
                dto.setPartnerName(senderDb.getFullName());
                dto.setPartnerAvatar("uploads/" + senderDb.getAvatar());
                dto.setPartnerOnline(senderDb.getIsOnline());
                dto.setUserType(conv.getUserType()); // ✅ thêm dòng này

            }
        }

        return dto;
    }

    @Override
    public Integer findStaffIdByConversationId(Integer conversationId) {
        List<StaffConversation> scList = staffConversationRepository
                .findByConversation_ConversationId(conversationId);
        if (scList.isEmpty()) {
            throw new RuntimeException("Staff conversation not found");
        }
        StaffConversation latest = scList.stream()
                .max(Comparator.comparing(StaffConversation::getAssignedAt))
                .orElseThrow(() -> new RuntimeException("No valid staff conversation found"));
        return latest.getStaff().getStaffId();
    }

    @Override
    public List<ConversationDTO> getAllPartnerConversationsByUserId(Integer partnerUserId) {
        List<Conversation> conversations = conversationRepository.findBySender_UserIdAndUserType(partnerUserId, "partner");

        return conversations.stream().map(conv -> {
            ConversationDTO dto = new ConversationDTO();
            dto.setConversationId(conv.getConversationId());
            dto.setSubject(conv.getSubject());
            dto.setSenderId(conv.getSender() != null ? conv.getSender().getUserId() : null);
            dto.setIsClosed(conv.getIsClosed());
            dto.setUpdatedAt(conv.getUpdatedAt());
            dto.setUserType(conv.getUserType()); // ✅ thêm dòng này


            if (conv.getSender() != null) {
                User senderDb = userRepository.findById(conv.getSender().getUserId()).orElse(null);
                if (senderDb != null) {
                    dto.setPartnerName(senderDb.getFullName());
                    dto.setPartnerAvatar("uploads/" + senderDb.getAvatar());
                    dto.setPartnerOnline(senderDb.getIsOnline());
                }
            }

            return dto;
        }).toList();
    }
}
