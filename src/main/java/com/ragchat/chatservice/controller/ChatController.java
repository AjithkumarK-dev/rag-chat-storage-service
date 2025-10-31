package com.ragchat.chatservice.controller;

import com.ragchat.chatservice.dto.ApiResponseDTO;
import com.ragchat.chatservice.dto.ChatSessionDTO;
import com.ragchat.chatservice.dto.MessageDTO;
import com.ragchat.chatservice.exception.ResourceNotFoundException;
import com.ragchat.chatservice.service.AIResponseService;
import com.ragchat.chatservice.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    @Autowired
    private AIResponseService aiResponseService;

    // -------------------- SESSION MANAGEMENT --------------------

    @PostMapping("/session")
    @Operation(summary = "Create a new chat session", tags = {"Sessions"})
    public ResponseEntity<ApiResponseDTO> createSession(@Valid @RequestBody ChatSessionDTO dto) {
        log.info("Creating session for userId={}", dto.getUserId());
        return ResponseEntity.ok(chatService.createSession(dto));
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Get chat session details by ID", tags = {"Sessions"})
    public ResponseEntity<ApiResponseDTO> getSession(@PathVariable UUID sessionId) {
        ChatSessionDTO session = chatService.getSessionById(sessionId);
        if (session == null) {
            throw new ResourceNotFoundException("Chat session not found for ID: " + sessionId);
        }
        return ResponseEntity.ok(new ApiResponseDTO(200, "Session fetched successfully", session));
    }

    @PutMapping("/session/{sessionId}")
    @Operation(summary = "Update chat session name or favorite flag", tags = {"Sessions"})
    public ResponseEntity<ApiResponseDTO> updateSession(
            @Parameter(description = "Session ID to update") @PathVariable UUID sessionId,
            @RequestBody ChatSessionDTO dto) {
        return ResponseEntity.ok(chatService.updateSession(sessionId, dto));
    }

    @DeleteMapping("/session/{sessionId}")
    @Operation(summary = "Delete chat session and its messages", tags = {"Sessions"})
    public ResponseEntity<ApiResponseDTO> deleteSession(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(chatService.deleteSession(sessionId));
    }

    @PatchMapping("/session/{sessionId}/favorite")
    @Operation(summary = "Toggle favorite status of a chat session", tags = {"Sessions"})
    public ResponseEntity<ApiResponseDTO> toggleFavorite(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(chatService.toggleFavorite(sessionId));
    }

    // -------------------- MESSAGE MANAGEMENT --------------------

    @PostMapping("/session/{sessionId}/message")
    @Operation(summary = "Add a message to a chat session", tags = {"Messages"})
    public ResponseEntity<ApiResponseDTO> addMessage(
            @PathVariable UUID sessionId,
            @Valid @RequestBody MessageDTO dto) {
        return ResponseEntity.ok(chatService.addMessage(sessionId, dto));
    }

    @GetMapping("/session/{sessionId}/messages")
    @Operation(summary = "Retrieve messages for a session (with pagination)", tags = {"Messages"})
    public ResponseEntity<ApiResponseDTO> getMessages(
            @Parameter(description = "Unique chat session ID")
            @PathVariable UUID sessionId,

            @Parameter(description = "Page number (starting from 0)", example = "0", schema = @io.swagger.v3.oas.annotations.media.Schema(type = "integer", format = "int64"))
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of records per page", example = "10", schema = @io.swagger.v3.oas.annotations.media.Schema(type = "integer", format = "int64"))
            @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching messages for sessionId={}, page={}, size={}", sessionId, page, size);
        ApiResponseDTO response = chatService.getMessages(sessionId, page, size);
        return ResponseEntity.ok(response);
    }


    // -------------------- GroqAI CHAT --------------------

    @PostMapping("/sessions/{sessionId}/chat")
    @Operation(summary = "Chat with Groq AI (stores both user and AI messages)", tags = {"Groq AI"})
    public ResponseEntity<ApiResponseDTO> chatWithGroqAI(
            @PathVariable UUID sessionId,
            @Valid @RequestBody MessageDTO messageDTO) {

        ChatSessionDTO session = chatService.getSessionById(sessionId);
        if (session == null)
            throw new ResourceNotFoundException("Chat session not found for ID: " + sessionId);

        // Save user message
        messageDTO.setSender("user");
        chatService.addMessage(sessionId, messageDTO);

        String aiReply;
        try {
            aiReply = aiResponseService.getAIResponse(
                    Map.of("messages", List.of(
                            Map.of("role", "user", "content", messageDTO.getMessage())
                    ))
            );
        } catch (Exception e) {
            log.error("Error communicating with Groq AI: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ApiResponseDTO(502, "Failed to get response from Groq AI", e.getMessage()));
        }

        // Save AI response
        MessageDTO aiMessage = new MessageDTO();
        aiMessage.setSender("assistant");
        aiMessage.setMessage(aiReply);
        chatService.addMessage(sessionId, aiMessage);

        return ResponseEntity.ok(
                new ApiResponseDTO(200, "Chat response generated successfully", aiMessage)
        );
    }

    // -------------------- ADMIN UTILITY (Protected via API Key) --------------------

    @PostMapping("/admin/clear-caches")
    @Operation(
            summary = "Clear all application caches",
            description = "Clears all in-memory caches such as chatSessions, chatMessages, etc.", tags = {"Clear Cache"}
    )
    public ResponseEntity<ApiResponseDTO> clearAllCaches() {
        ApiResponseDTO response = chatService.clearAllCaches();
        return ResponseEntity.ok(response);
    }
}