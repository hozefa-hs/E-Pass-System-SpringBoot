package com.porfolio.EPassSystemSpringboot.entities;

import com.porfolio.EPassSystemSpringboot.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "unique_username", columnNames = "username"))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder

public class Users implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String username;
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<Pass> passes = new ArrayList<>();

    @OneToMany(mappedBy = "passenger")
    private List<PassApplication> applications = new ArrayList<>();


    //This method tells spring that which authority (roles & permissions) does this current user has.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of(
          new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}










