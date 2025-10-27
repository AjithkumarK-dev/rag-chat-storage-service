package com.ragchat.chatservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDTO {

    @Schema(description = "HTTP status code", example = "200")
    private int code;

    @Schema(description = "Descriptive response message", example = "Operation successful")
    private String message;

    @Schema(description = "Payload containing the actual data", example = "{...}")
    private Object data;
}
