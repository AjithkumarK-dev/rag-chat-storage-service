package com.ragchat.chatservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AIResponseService {

    private final WebClient webClient;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    public AIResponseService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .build();
    }

    /**
     * Sends a prompt to OpenAI and returns the generated response text.
     * Retries automatically and uses a circuit breaker as defined in application.yml.
     */
    @Retry(name = "openaiRetry", fallbackMethod = "fallbackResponse")
    @CircuitBreaker(name = "openaiCB", fallbackMethod = "fallbackResponse")
    public String getAIResponse(Map<String, Object> requestBody) {
        try {
            Map<String, Object> request = Map.of(
                    "model", "gpt-3.5-turbo",
                    "messages", requestBody.get("messages"),
                    "temperature", 0.7
            );

            Map<String, Object> response = webClient.post()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorResume(e -> {
                        log.error("Error calling OpenAI API: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (response == null || !response.containsKey("choices")) {
                log.warn("No valid response received from OpenAI.");
                return "No response received from OpenAI.";
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices.isEmpty()) {
                return "No choices returned by OpenAI.";
            }

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return message != null
                    ? message.get("content").toString().trim()
                    : "Empty AI message.";

        } catch (Exception e) {
            log.error("Exception while fetching AI response: {}", e.getMessage());
            return "Error communicating with OpenAI API.";
        }
    }

    /**
     * Fallback executed when OpenAI service fails repeatedly or circuit is open.
     */
    private String fallbackResponse(Map<String, Object> requestBody, Throwable t) {
        log.error("OpenAI service fallback triggered: {}", t.getMessage());
        return "OpenAI service is temporarily unavailable. Please try again later.";
    }
}
