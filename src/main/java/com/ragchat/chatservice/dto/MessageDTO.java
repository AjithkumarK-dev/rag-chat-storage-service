package com.ragchat.chatservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class MessageDTO {

    @Schema(description = "Unique message ID", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

  /*  @NotNull(message = "Session ID is required")
    @Schema(description = "ID of the chat session this message belongs to", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID sessionId;*/

    @NotBlank(message = "Sender is required")
    @Pattern(regexp = "^(user|assistant)$", message = "Sender must be either 'user' or 'assistant'")
    @Schema(description = "Message sender (user or assistant)", example = "user")
    private String sender;

    @NotBlank(message = "Message content cannot be empty")
    @Size(max = 2000, message = "Message cannot exceed 2000 characters")
    @Schema(description = "Message text", example = "Hello, how can I check my account balance?")
    private String message;
}
