package com.ragchat.chatservice.service;

import com.ragchat.chatservice.dto.ApiResponseDTO;
import com.ragchat.chatservice.dto.ChatSessionDTO;
import com.ragchat.chatservice.dto.MessageDTO;
import com.ragchat.chatservice.exception.ResourceNotFoundException;
import com.ragchat.chatservice.model.ChatMessage;
import com.ragchat.chatservice.model.ChatSession;
import com.ragchat.chatservice.repository.ChatMessageRepository;
import com.ragchat.chatservice.repository.ChatSessionRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CacheManager cacheManager;

    /**
     * ✅ Create a new chat session
     */
    @Transactional
    @CachePut(value = "chatSessions", key = "#result.data.id")
    public ApiResponseDTO createSession(ChatSessionDTO dto) {
        log.info("Creating chat session for user {}", dto.getUserId());
        ChatSession session = modelMapper.map(dto, ChatSession.class);
        ChatSession saved = chatSessionRepository.saveAndFlush(session);
        ChatSessionDTO response = modelMapper.map(saved, ChatSessionDTO.class);
        log.debug("Session created with ID {}", saved.getId());
        return new ApiResponseDTO(200, "Session creation completed", response);
    }

    /**
     * ✅ Get session details by ID (returns ChatSessionDTO for controller compatibility)
     */
    @Cacheable(value = "chatSessionById", key = "#sessionId")
    public ChatSessionDTO getSessionById(UUID sessionId) {
        log.debug("Fetching session with ID {}", sessionId);
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found: " + sessionId));
        return modelMapper.map(session, ChatSessionDTO.class);
    }

    /**
     * ✅ Get all sessions for a specific user
     */
    @Cacheable(value = "chatSessionsByUser", key = "#userId")
    public ApiResponseDTO getAllSessions(String userId) {
        log.debug("Fetching sessions from DB for user {}", userId);
        List<ChatSessionDTO> sessions = chatSessionRepository.findByUserId(userId)
                .stream()
                .map(s -> modelMapper.map(s, ChatSessionDTO.class))
                .collect(Collectors.toList());
        return new ApiResponseDTO(200, "Sessions fetched successfully", sessions);
    }

    /**
     * ✅ Update session name or favorite status
     */
    @Transactional
    @CacheEvict(value = {"chatSessions", "chatSessionsByUser", "chatSessionById"}, allEntries = true)
    public ApiResponseDTO updateSession(UUID sessionId, ChatSessionDTO dto) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id " + sessionId));

        // Validation
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            log.warn("Session name missing for update on sessionId={}", sessionId);
            return new ApiResponseDTO(400, "Session name is required for update", null);
        }

        session.setName(dto.getName());
        session.setFavorite(dto.isFavorite());
        ChatSession saved = chatSessionRepository.save(session);
        ChatSessionDTO updated = modelMapper.map(saved, ChatSessionDTO.class);

        log.info("✏️ Updated session for sessionId={}, name='{}', favorite={}",
                sessionId, dto.getName(), dto.isFavorite());

        return new ApiResponseDTO(200, "Session updated successfully", updated);
    }

    /**
     * ✅ Delete a session and its messages
     */
    @Transactional
    @CacheEvict(value = {"chatSessions", "chatSessionsByUser", "chatSessionById", "chatMessages"}, allEntries = true)
    public ApiResponseDTO deleteSession(UUID sessionId) {
        log.warn("Deleting session {}", sessionId);
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id " + sessionId));

        chatMessageRepository.deleteBySessionId(sessionId);
        chatSessionRepository.delete(session);

        log.info("🗑️ Session {} and related messages deleted", sessionId);
        return new ApiResponseDTO(200, "Session deleted successfully", null);
    }

    /**
     * ✅ Add message to a chat session
     */
    @Transactional
    @CacheEvict(value = "chatMessages", key = "#sessionId")
    public ApiResponseDTO addMessage(UUID sessionId, MessageDTO dto) {
        if (!chatSessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Session not found with id " + sessionId);
        }

        ChatMessage message = modelMapper.map(dto, ChatMessage.class);
        message.setSessionId(sessionId);

        ChatMessage saved = chatMessageRepository.saveAndFlush(message);
        MessageDTO response = modelMapper.map(saved, MessageDTO.class);

        return new ApiResponseDTO(200, "Message added successfully", response);
    }

    /**
     * ✅ Get messages for a session (with pagination)
     */
    @Cacheable(value = "chatMessages", key = "#sessionId + '-' + #page + '-' + #size")
    public ApiResponseDTO getMessages(UUID sessionId, int page, int size) {
        if (!chatSessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Session not found with id " + sessionId);
        }

        if (page > 0) page = page - 1;

        List<MessageDTO> messages = chatMessageRepository
                .findBySessionId(sessionId, PageRequest.of(page, size, Sort.by("createdAt").ascending()))
                .stream()
                .map(msg -> modelMapper.map(msg, MessageDTO.class))
                .collect(Collectors.toList());

        log.debug("Fetched {} messages for session {}", messages.size(), sessionId);
        return new ApiResponseDTO(200, "Messages retrieved successfully", messages);
    }

    /**
     * ✅ Toggle favorite status
     */
    @Transactional
    @CacheEvict(value = {"chatSessions", "chatSessionsByUser", "chatSessionById"}, allEntries = true)
    public ApiResponseDTO toggleFavorite(UUID sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id " + sessionId));

        session.setFavorite(!session.isFavorite());
        chatSessionRepository.save(session);

        ChatSessionDTO dto = modelMapper.map(session, ChatSessionDTO.class);
        log.info("⭐ Favorite toggled for session {} -> {}", sessionId, session.isFavorite());
        return new ApiResponseDTO(200, "Favorite toggled successfully", dto);
    }

    // ✅ Clear all caches programmatically
    public ApiResponseDTO clearAllCaches() {
        cacheManager.getCacheNames().forEach(name -> {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
                log.info("Cleared cache: {}", name);
            }
        });
        return new ApiResponseDTO(200, "All caches cleared successfully", null);
    }
}
