package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.entity.Conversation;
import com.livewithoutthinking.resq.repository.ConversationRepository;
import com.livewithoutthinking.resq.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationServiceImpl implements ConversationService {
    @Autowired
    private ConversationRepository conversationRepository;

    public List<Conversation> findByStaff(int staffId) {
        return conversationRepository.findConversationsByStaffId(staffId);
    }
}
