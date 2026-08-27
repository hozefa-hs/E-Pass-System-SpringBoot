package com.porfolio.EPassSystemSpringboot.dtos;

import com.porfolio.EPassSystemSpringboot.enums.Role;
import lombok.*;

//UserResponseDto is used to give response when user is successfully registered.

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private Long id;
    private String username;
    private String password;
    private Role role;
}
