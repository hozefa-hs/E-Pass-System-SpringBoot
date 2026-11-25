package com.porfolio.EPassSystemSpringboot.services;

import com.porfolio.EPassSystemSpringboot.dtos.AiChatRequestDto;
import com.porfolio.EPassSystemSpringboot.dtos.AiChatResponseDto;

public interface AiAssistantService {

    AiChatResponseDto aiAssistantChat(AiChatRequestDto requestDto);

}
