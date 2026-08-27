package com.porfolio.EPassSystemSpringboot.entities;

import com.porfolio.EPassSystemSpringboot.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder

public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String username;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;


}










