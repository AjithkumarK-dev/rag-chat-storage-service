package com.ragchat.chatservice.repository;

import com.ragchat.chatservice.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    // Existing method for ordered retrieval
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    // New method to support pagination
    Page<ChatMessage> findBySessionId(UUID sessionId, Pageable pageable);

    // Optional: To support delete by sessionId
    void deleteBySessionId(UUID sessionId);
}
