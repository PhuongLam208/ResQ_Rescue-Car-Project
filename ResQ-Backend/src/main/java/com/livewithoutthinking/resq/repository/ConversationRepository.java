package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.Conversation;
import com.livewithoutthinking.resq.entity.Staff;
import com.livewithoutthinking.resq.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Integer> {

    // ConversationRepository.java
    Optional<Conversation> findFirstBySender_UserIdOrderByUpdatedAtDesc(Integer userId);

    List<Conversation> findBySender_UserId(Integer userId);

    // Tìm 1 cuộc trò chuyện giữa 1 User (Sender) và 1 Staff (Recipient)
    //Optional<Conversation> findBySender_UserIdAndRecipient_StaffId(Integer senderUserId, Integer recipientStaffId);

    // Tìm tất cả cuộc trò chuyện mà user đó là staff (recipient)
    //List<Conversation> findByRecipient_User_UserId(Integer userId);

    List<Conversation> findBySender(User sender);
    //List<Conversation> findByRecipient(Staff recipient);

    @Query("SELECT sc.conversation FROM StaffConversation sc WHERE sc.staff.staffId = :staffId")
    List<Conversation> findConversationsByStaffId(@Param("staffId") int staffId);

    List<Conversation> findBySender_UserIdAndUserType(Integer senderId, String userType);
}
