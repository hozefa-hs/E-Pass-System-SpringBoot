package com.porfolio.EPassSystemSpringboot.services;


import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.enums.Role;
import com.porfolio.EPassSystemSpringboot.exceptions.ResourceNotFoundException;
import com.porfolio.EPassSystemSpringboot.repositories.UserRepository;
import com.porfolio.EPassSystemSpringboot.services.implementations.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    private static final Long USER_ID = 10L;
    private static final String USERNAME = "hozefa";
    private static final String PASSWORD = "encoded-password";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;


    @Test
    void loadUserByUsername_existingUser_returnsExactUserDetails() {

        // Arrange
        Users user = userWithRole(Role.PASSENGER);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(USERNAME);

        // Assert
        assertSame(user, result);

        assertEquals(USERNAME, result.getUsername());

        assertEquals(PASSWORD, result.getPassword());

        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void loadUserByUsername_passenger_returnsPassengerAuthority() {

        // Arrange
        Users user = userWithRole(Role.PASSENGER);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(USERNAME);

        // Assert
        Set<String> authorities = result.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        assertEquals(Set.of("ROLE_PASSENGER"), authorities);

        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void loadUserByUsername_admin_returnsAdminAuthority() {

        // Arrange
        Users user = userWithRole(Role.ADMIN);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(USERNAME);

        // Assert
        Set<String> authorities = result.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        assertEquals(Set.of("ROLE_ADMIN"), authorities);

        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void loadUserByUsername_passOfficer_returnsPassOfficerAuthority() {

        // Arrange
        Users user = userWithRole(Role.PASS_OFFICER);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(USERNAME);

        // Assert
        Set<String> authorities = result.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        assertEquals(Set.of("ROLE_PASS_OFFICER"), authorities);

        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void loadUserByUsername_ticketChecker_returnsTicketCheckerAuthority() {

        // Arrange
        Users user = userWithRole(Role.TICKET_CHECKER);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(USERNAME);

        // Assert
        Set<String> authorities = result.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        assertEquals(Set.of("ROLE_TICKET_CHECKER"), authorities);

        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void loadUserByUsername_usernameNotFound_throwsResourceNotFound() {

        // Arrange
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> customUserDetailsService.loadUserByUsername(USERNAME));

        // Assert
        assertEquals("Username not found", exception.getMessage());

        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void loadUserByUsername_nullUsername_currentBehaviorQueriesRepositoryAndThrowsResourceNotFound() {

        // Arrange
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> customUserDetailsService.loadUserByUsername(null));

        // Assert
        assertEquals("Username not found", exception.getMessage());

        verify(userRepository).findByUsername(null);
    }


    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Users userWithRole(Role role) {

        return Users.builder().userId(USER_ID).username(USERNAME).password(PASSWORD).role(role).build();
    }
}