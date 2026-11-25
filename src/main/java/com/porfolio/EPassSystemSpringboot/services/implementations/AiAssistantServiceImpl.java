package com.porfolio.EPassSystemSpringboot.services.implementations;

import com.porfolio.EPassSystemSpringboot.configs.AiConfig;
import com.porfolio.EPassSystemSpringboot.dtos.AiChatRequestDto;
import com.porfolio.EPassSystemSpringboot.dtos.AiChatResponseDto;
import com.porfolio.EPassSystemSpringboot.services.AiAssistantService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private final ChatClient chatClient;

    @Override
    public AiChatResponseDto aiAssistantChat(AiChatRequestDto requestDto) {

        String reply = chatClient.prompt(requestDto.getMessage()).call().content();

        AiChatResponseDto responseDto = new AiChatResponseDto();
        responseDto.setReply(reply);

        return responseDto;

    }
}
