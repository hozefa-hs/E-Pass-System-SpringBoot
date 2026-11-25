package com.porfolio.EPassSystemSpringboot.dtos;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AiChatResponseDto {

    private UUID conversationId;
    private String reply;

}
