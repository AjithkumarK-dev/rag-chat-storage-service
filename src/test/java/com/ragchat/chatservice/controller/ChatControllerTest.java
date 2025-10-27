package com.ragchat.chatservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ragchat.chatservice.dto.ApiResponseDTO;
import com.ragchat.chatservice.dto.ChatSessionDTO;
import com.ragchat.chatservice.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ChatControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateSession() throws Exception {
        ChatSessionDTO dto = new ChatSessionDTO();
        dto.setUserId("123451");
        dto.setName("Support Chat");

        ApiResponseDTO response = new ApiResponseDTO(200, "Session creation completed", dto);

        when(chatService.createSession(any(ChatSessionDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/chat/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Session creation completed"));
    }

    @Test
    void testUpdateSession() throws Exception {
        ChatSessionDTO dto = new ChatSessionDTO();
        dto.setName("Updated Chat");
        ApiResponseDTO response = new ApiResponseDTO(200, "Session updated successfully", dto);

        when(chatService.updateSession(any(UUID.class), any(ChatSessionDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/chat/session/{sessionId}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Session updated successfully"));
    }

    @Test
    void testDeleteSession() throws Exception {
        ApiResponseDTO response = new ApiResponseDTO(200, "Session deleted successfully", null);
        when(chatService.deleteSession(any(UUID.class))).thenReturn(response);

        mockMvc.perform(delete("/api/chat/session/{sessionId}", UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Session deleted successfully"));
    }
}