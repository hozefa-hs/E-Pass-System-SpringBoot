package com.porfolio.EPassSystemSpringboot.dtos;

//Register User Request Dto

import com.porfolio.EPassSystemSpringboot.enums.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserDto {

    @NotBlank(message = "username cannot be blank")
    private String username;

    @NotBlank(message = "password cannot be blank")
    private String password;

}
