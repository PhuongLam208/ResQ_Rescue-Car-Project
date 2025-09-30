package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.Conversation;
import com.livewithoutthinking.resq.entity.Message;
import com.livewithoutthinking.resq.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    // Lấy tất cả tin nhắn trong 1 conversation theo thời gian
    List<Message> findByConversationOrderByCreatedAtAsc(Conversation conversation);

    // Lấy tin nhắn chưa đọc
    List<Message> findByConversationAndStatus(Conversation conversation, Message.Status status);

    // Lấy tin nhắn gửi bởi 1 người
    List<Message> findBySender(User sender);

    @Query("SELECT m FROM Message m WHERE m.conversation.conversationId = :conversationId")
    List<Message> findByConversation(int conversationId);

}
