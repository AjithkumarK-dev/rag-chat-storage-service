package com.ragchat.chatservice.service;

import com.ragchat.chatservice.dto.ApiResponseDTO;
import com.ragchat.chatservice.dto.ChatSessionDTO;
import com.ragchat.chatservice.exception.ResourceNotFoundException;
import com.ragchat.chatservice.model.ChatSession;
import com.ragchat.chatservice.repository.ChatSessionRepository;
import com.ragchat.chatservice.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ChatServiceTest {

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ChatService chatService;

    private ChatSession session;
    private ChatSessionDTO dto;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        session = new ChatSession();
        session.setId(UUID.randomUUID());
        session.setUserId("123451");
        session.setName("Support Chat");

        dto = new ChatSessionDTO();
        dto.setUserId("123451");
        dto.setName("Support Chat");
        dto.setFavorite(false);
    }

    @Test
    void testCreateSessionSuccess() {
        when(modelMapper.map(dto, ChatSession.class)).thenReturn(session);
        when(chatSessionRepository.saveAndFlush(any(ChatSession.class))).thenReturn(session);
        when(modelMapper.map(session, ChatSessionDTO.class)).thenReturn(dto);

        ApiResponseDTO response = chatService.createSession(dto);

        assertEquals(200, response.getCode());
        assertEquals("Session creation completed", response.getMessage());
    }

    @Test
    void testUpdateSession_UserIdCannotChange() {
        when(chatSessionRepository.findById(session.getId())).thenReturn(Optional.of(session));

        ChatSessionDTO newDto = new ChatSessionDTO();
        newDto.setUserId("999999");
        newDto.setName("Changed Chat");

        ApiResponseDTO response = chatService.updateSession(session.getId(), newDto);

        assertEquals(400, response.getCode());
        assertEquals("User ID cannot be changed for an existing session", response.getMessage());
    }

    @Test
    void testUpdateSession_ValidUpdate() {
        when(chatSessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(session);
        when(modelMapper.map(session, ChatSessionDTO.class)).thenReturn(dto);

        dto.setName("Updated Chat");
        ApiResponseDTO response = chatService.updateSession(session.getId(), dto);

        assertEquals(200, response.getCode());
        assertEquals("Session updated successfully", response.getMessage());
    }

    @Test
    void testGetSession_NotFound() {
        when(chatSessionRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                chatService.getSessionById(UUID.randomUUID()));
    }

    @Test
    void testDeleteSession_Success() {
        when(chatSessionRepository.findById(session.getId())).thenReturn(Optional.of(session));

        ApiResponseDTO response = chatService.deleteSession(session.getId());

        assertEquals(200, response.getCode());
        assertEquals("Session deleted successfully", response.getMessage());
        verify(chatMessageRepository, times(1)).deleteBySessionId(session.getId());
        verify(chatSessionRepository, times(1)).delete(session);
    }
}