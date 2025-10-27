package com.ragchat.chatservice.repository;

import com.ragchat.chatservice.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

    // Get all sessions for a specific user
    List<ChatSession> findByUserId(String userId);
}
