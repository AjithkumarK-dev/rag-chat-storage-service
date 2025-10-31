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

    @Value("${groq.api.key}")
    private String groqApiKey;

    public AIResponseService(WebClient.Builder webClientBuilder) {
        //Use Groq's OpenAI-compatible endpoint
        this.webClient = webClientBuilder
                .baseUrl("https://api.groq.com/openai/v1")
                .build();
    }

    /**
     * Sends a prompt to Groq (Llama 3.1) and returns the generated response text.
     * Includes retry and circuit breaker mechanisms.
     */
    @Retry(name = "groqRetry", fallbackMethod = "fallbackResponse")
    @CircuitBreaker(name = "groqCB", fallbackMethod = "fallbackResponse")
    public String getAIResponse(Map<String, Object> requestBody) {
        try {
            Map<String, Object> request = Map.of(
                    "model", "llama-3.1-8b-instant",
                    "messages", requestBody.get("messages"),
                    "temperature", 0.7
            );

            log.info("Sending prompt to Groq API...");

            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + groqApiKey)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorResume(e -> {
                        log.error("Error calling Groq API: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (response == null || !response.containsKey("choices")) {
                log.warn("No valid response received from Groq.");
                return "No response received from Groq.";
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices.isEmpty()) {
                return "No choices returned by Groq.";
            }

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return message != null
                    ? message.get("content").toString().trim()
                    : "Empty AI message.";

        } catch (Exception e) {
            log.error("Exception while fetching AI response: {}", e.getMessage());
            return "Error communicating with Groq API.";
        }
    }

    /**
     * Fallback executed when Groq service fails repeatedly or circuit is open.
     */
    private String fallbackResponse(Map<String, Object> requestBody, Throwable t) {
        log.error("Groq service fallback triggered: {}", t.getMessage());
        return "Groq AI service is temporarily unavailable. Please try again later.";
    }
}