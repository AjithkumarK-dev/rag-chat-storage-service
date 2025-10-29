package com.ragchat.chatservice.config;

import com.ragchat.chatservice.repository.ChatSessionRepository;
import com.ragchat.chatservice.repository.ChatMessageRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DataInitializer ensures the application starts correctly by verifying database connectivity.
 * It does NOT insert any demo data.
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(ChatSessionRepository sessionRepo, ChatMessageRepository messageRepo) {
        return args -> {
            System.out.println("Application started successfully with PostgreSQL connection.");

            long sessionCount = sessionRepo.count();
            long messageCount = messageRepo.count();

            System.out.println("Current records in database:");
            System.out.println("Chat Sessions: " + sessionCount);
            System.out.println("Chat Messages: " + messageCount);
        };
    }
}