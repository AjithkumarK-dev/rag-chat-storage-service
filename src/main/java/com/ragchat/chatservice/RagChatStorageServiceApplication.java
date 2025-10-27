package com.ragchat.chatservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class RagChatStorageServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RagChatStorageServiceApplication.class, args);
    }
}