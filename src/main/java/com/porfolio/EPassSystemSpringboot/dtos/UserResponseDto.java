package com.porfolio.EPassSystemSpringboot.dtos;

import com.porfolio.EPassSystemSpringboot.enums.Role;
import lombok.*;

//UserResponseDto is used to give response when user is successfully registered.

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {

    private Long userId;
    private String username;
    private Role role;
}
