package com.livewithoutthinking.resq.service;

import com.livewithoutthinking.resq.entity.Conversation;

import java.util.List;

public interface ConversationService {
    List<Conversation> findByStaff(int staffId);
}
