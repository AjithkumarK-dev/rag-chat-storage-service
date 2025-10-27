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
@Tag(name = "Chat Controller", description = "Endpoints for managing chat sessions and messages")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    @Autowired
    private AIResponseService aiResponseService;


    // ✅ CREATE SESSION
    @PostMapping("/session")
    @Operation(summary = "Create a new chat session")
    public ResponseEntity<ApiResponseDTO> createSession(@Valid @RequestBody ChatSessionDTO dto) {
        log.info("Received request to create session for userId={}", dto.getUserId());
        ApiResponseDTO response = chatService.createSession(dto);
        return ResponseEntity.ok(response);
    }

    // ✅ DELETE SESSION
    @DeleteMapping("/session/{sessionId}")
    @Operation(summary = "Delete a chat session and its messages")
    public ResponseEntity<ApiResponseDTO> deleteSession(@PathVariable UUID sessionId) {
        log.info("Deleting session with ID={}", sessionId);
        ApiResponseDTO response = chatService.deleteSession(sessionId);
        return ResponseEntity.ok(response);
    }

    // ✅ UPDATE SESSION
    // ✅ UPDATE SESSION (no @Valid → validation handled inside service)
    @PutMapping("/session/{sessionId}")
    @Operation(
            summary = "Update session name or favorite flag",
            description = "Updates session details. User ID cannot be changed. "
                    + "Name is mandatory for update. Favorite is optional."
    )
    public ResponseEntity<ApiResponseDTO> updateSession(
            @Parameter(description = "Session ID to update")
            @PathVariable UUID sessionId,
            @RequestBody ChatSessionDTO dto) {  // ✅ Removed @Valid here
        log.info("Updating session with ID={}", sessionId);
        ApiResponseDTO response = chatService.updateSession(sessionId, dto);
        return ResponseEntity.ok(response);
    }

    // ✅ GET SESSION DETAILS
    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Get session details by ID")
    public ResponseEntity<ApiResponseDTO> getSession(@PathVariable UUID sessionId) {
        log.info("Fetching session details for ID={}", sessionId);
        ApiResponseDTO response = chatService.getSession(sessionId);
        return ResponseEntity.ok(response);
    }

    // ✅ ADD MESSAGE
    @PostMapping("/session/{sessionId}/message")
    @Operation(summary = "Add a message to a chat session")
    public ResponseEntity<ApiResponseDTO> addMessage(
            @PathVariable UUID sessionId,
            @Valid @RequestBody MessageDTO dto) {
        log.info("Adding message to sessionId={}", sessionId);
        ApiResponseDTO response = chatService.addMessage(sessionId, dto);
        return ResponseEntity.ok(response);
    }

    // ✅ GET MESSAGES
    @GetMapping("/session/{sessionId}/messages")
    @Operation(summary = "Retrieve messages for a session with pagination")
    public ResponseEntity<ApiResponseDTO> getMessages(
            @PathVariable UUID sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching messages for sessionId={}, page={}, size={}", sessionId, page, size);
        ApiResponseDTO response = chatService.getMessages(sessionId, page, size);
        return ResponseEntity.ok(response);
    }

    // ✅ TOGGLE FAVORITE
    @PatchMapping("/session/{sessionId}/favorite")
    @Operation(summary = "Toggle favorite status of a session")
    public ResponseEntity<ApiResponseDTO> toggleFavorite(@PathVariable UUID sessionId) {
        log.info("Toggling favorite for sessionId={}", sessionId);
        ApiResponseDTO response = chatService.toggleFavorite(sessionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sessions/{sessionId}/chat")
    @Operation(summary = "Chat with OpenAI", description = "Sends a message to OpenAI and stores both user and AI responses under the session")
    public ResponseEntity<ApiResponseDTO> chatWithOpenAI(
            @PathVariable UUID sessionId,
            @Valid @RequestBody MessageDTO messageDTO) {

        // ✅ Step 1: Validate session existence
        ChatSessionDTO session = chatService.getSessionById(sessionId);
        if (session == null) {
            throw new ResourceNotFoundException("Chat session not found for ID: " + sessionId);
        }

        // ✅ Step 2: Save the user message
        messageDTO.setSender("user");
        chatService.addMessage(sessionId, messageDTO);

        // ✅ Step 3: Call OpenAI to get response
        String aiReply;
        try {
            aiReply = aiResponseService.getAIResponse(
                    Map.of("messages", List.of(
                            Map.of("role", "user", "content", messageDTO.getMessage())
                    ))
            );
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body(new ApiResponseDTO(502, "Failed to get response from OpenAI", e.getMessage()));
        }

        // ✅ Step 4: Save AI message
        MessageDTO aiMessage = new MessageDTO();
        aiMessage.setSender("assistant");
        aiMessage.setMessage(aiReply);
        chatService.addMessage(sessionId, aiMessage);

        // ✅ Step 5: Return response
        return ResponseEntity.ok(
                new ApiResponseDTO(200, "Chat response generated successfully", aiMessage)
        );
    }
}