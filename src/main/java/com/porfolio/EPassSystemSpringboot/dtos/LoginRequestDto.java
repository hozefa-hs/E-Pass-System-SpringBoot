package com.porfolio.EPassSystemSpringboot.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDto {

    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "password id required")
    private String password;
}
