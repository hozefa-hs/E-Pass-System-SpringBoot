package com.porfolio.EPassSystemSpringboot.dtos;

import com.porfolio.EPassSystemSpringboot.enums.Role;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {

    private String jwtToken;
    private String username;
    private Role role;
}
