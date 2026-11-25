package com.porfolio.EPassSystemSpringboot.configs;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class AiConfig {

    @Value("classpath:prompts/e-pass-assistant.txt")
    private Resource systemPromptResource;

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) throws IOException {

        String systemPrompt = systemPromptResource.getContentAsString(StandardCharsets.UTF_8);

        return builder
                .defaultSystem(systemPrompt)
                .build();
    }

}
