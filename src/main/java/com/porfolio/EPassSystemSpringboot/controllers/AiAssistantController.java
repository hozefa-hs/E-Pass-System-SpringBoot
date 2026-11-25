package com.porfolio.EPassSystemSpringboot.controllers;

import com.porfolio.EPassSystemSpringboot.dtos.AiChatRequestDto;
import com.porfolio.EPassSystemSpringboot.dtos.AiChatResponseDto;
import com.porfolio.EPassSystemSpringboot.services.AiAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponseDto> aiChat(@RequestBody AiChatRequestDto requestDto) {

        AiChatResponseDto responseDto = aiAssistantService.aiAssistantChat(requestDto);

        return ResponseEntity.ok(responseDto);

    }

}
