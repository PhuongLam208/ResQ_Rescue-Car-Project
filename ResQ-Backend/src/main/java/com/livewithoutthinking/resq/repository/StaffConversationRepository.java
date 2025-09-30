package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.StaffConversation;
import com.livewithoutthinking.resq.entity.StaffConversationId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaffConversationRepository extends JpaRepository<StaffConversation, StaffConversationId> {
    List<StaffConversation> findByStaff_User_UserId(Integer userId);
    Optional<StaffConversation> findByStaff_StaffIdAndConversation_Sender_UserId(Integer staffId, Integer senderUserId);
    List<StaffConversation> findByConversation_ConversationId(Integer conversationId);
    List<StaffConversation> findByStaff_StaffId(Integer staffId);
}
