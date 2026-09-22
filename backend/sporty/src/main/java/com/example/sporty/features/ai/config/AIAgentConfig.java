package com.example.sporty.features.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.sporty.features.ai.tools.MatchAITool;

@Configuration 
public class AIAgentConfig {
    
    // MatchAIAgent의 ChatClient 변수명과 같아야 함
    @Bean
    public ChatClient matchChatClient(ChatClient.Builder builder, MatchAITool matchAITool) {
        return builder.defaultTools(matchAITool).build();
    }
}
