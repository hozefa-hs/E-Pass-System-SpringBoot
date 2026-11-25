package com.porfolio.EPassSystemSpringboot.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AiChatRequestDto {

    @NotBlank(message = "Message to ask cannot be blank")
    @Size(max = 500, message = "Message cannot exceed 500 characters")
    private String message;

}
