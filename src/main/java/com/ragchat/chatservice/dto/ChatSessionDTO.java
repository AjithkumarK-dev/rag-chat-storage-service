package com.ragchat.chatservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ChatSessionDTO {

    @Schema(description = "Auto-generated session ID", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotBlank(message = "User ID is mandatory")
    @Size(min = 6, max = 6, message = "User ID must be exactly 6 digits long")
    @Pattern(regexp = "\\d{6}", message = "User ID must contain only digits")
    @Schema(description = "6-digit User ID of the session creator", example = "123451")
    private String userId;

    @NotBlank(message = "Session name is required")
    @Size(min = 3, max = 50, message = "Session name must be between 3 and 50 characters")
    @Schema(description = "Display name of the chat session", example = "Support Chat")
    private String name;

    @Schema(description = "Mark session as favorite (optional, defaults to false)", example = "false")
    private boolean favorite = false;

    @Schema(description = "Timestamp when the session was created", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the session was last updated", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}
