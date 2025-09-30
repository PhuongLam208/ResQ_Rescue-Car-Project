package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.dto.ConversationDTO;
import com.livewithoutthinking.resq.dto.MessageDTO;

import java.util.List;

public interface PartnerChatService {
    MessageDTO sendPartnerMessage(Integer staffId, Integer partnerUserId, String content);
    List<MessageDTO> getPartnerMessages(Integer conversationId);
    public ConversationDTO getConversationDTOById(Integer conversationId);
    //public Integer findStaffIdByConversationId(Integer conversationId);
    public List<ConversationDTO> getAllPartnerConversationsByUserId(Integer partnerUserId);
    Integer findStaffIdByConversationId(Integer conversationId);
}
