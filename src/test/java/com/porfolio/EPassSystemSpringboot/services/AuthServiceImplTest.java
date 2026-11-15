package com.porfolio.EPassSystemSpringboot.services;


import com.porfolio.EPassSystemSpringboot.dtos.LoginRequestDto;
import com.porfolio.EPassSystemSpringboot.dtos.LoginResponseDto;
import com.porfolio.EPassSystemSpringboot.dtos.RegisterUserDto;
import com.porfolio.EPassSystemSpringboot.dtos.UserResponseDto;
import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.enums.Role;
import com.porfolio.EPassSystemSpringboot.exceptions.BusinessException;
import com.porfolio.EPassSystemSpringboot.exceptions.ResourceNotFoundException;
import com.porfolio.EPassSystemSpringboot.repositories.UserRepository;
import com.porfolio.EPassSystemSpringboot.services.implementations.AuthServiceIMPL;
import com.porfolio.EPassSystemSpringboot.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final Long USER_ID = 10L;

    private static final String USERNAME = "hozefa";
    private static final String RAW_PASSWORD = "Password@123";
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final String JWT_TOKEN = "test.jwt.token";

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceIMPL authService;


    // -------------------------------------------------------------------------
    // registerPassenger
    // -------------------------------------------------------------------------

    @Test
    void registerPassenger_usernameAlreadyExists_throwsBusinessException() {

        // Arrange
        RegisterUserDto request = registerRequest();

        Users existingUser = userWithRole(Role.PASSENGER);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(existingUser));

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> authService.registerPassenger(request));

        // Assert
        assertEquals("Username already exists", exception.getMessage());

        verifyNoInteractions(passwordEncoder, modelMapper, authenticationManager, jwtUtil);

        verify(userRepository, never()).save(any(Users.class));
    }

    @Test
    void registerPassenger_validRequest_encodesPasswordAndAssignsPassengerRole() {

        // Arrange
        RegisterUserDto request = registerRequest();

        UserResponseDto expectedResponse = userResponse(Role.PASSENGER);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);

        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> {
            Users user = invocation.getArgument(0);
            user.setUserId(USER_ID);
            return user;
        });

        when(modelMapper.map(any(Users.class), org.mockito.ArgumentMatchers.eq(UserResponseDto.class))).thenReturn(expectedResponse);

        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);

        // Act
        UserResponseDto result = authService.registerPassenger(request);

        // Assert
        assertSame(expectedResponse, result);

        verify(passwordEncoder).encode(RAW_PASSWORD);

        verify(userRepository).save(userCaptor.capture());

        Users savedUser = userCaptor.getValue();

        assertEquals(USERNAME, savedUser.getUsername());

        assertEquals(ENCODED_PASSWORD, savedUser.getPassword());

        assertEquals(Role.PASSENGER, savedUser.getRole());

        verify(modelMapper).map(savedUser, UserResponseDto.class);

        verifyNoInteractions(authenticationManager, jwtUtil);
    }


    // -------------------------------------------------------------------------
    // registerPassOfficer
    // -------------------------------------------------------------------------

    @Test
    void registerPassOfficer_usernameAlreadyExists_throwsBusinessException() {

        // Arrange
        RegisterUserDto request = registerRequest();

        Users existingUser = userWithRole(Role.PASS_OFFICER);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(existingUser));

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> authService.registerPassOfficer(request));

        // Assert
        assertEquals("Username already exists", exception.getMessage());

        verifyNoInteractions(passwordEncoder, modelMapper, authenticationManager, jwtUtil);

        verify(userRepository, never()).save(any(Users.class));
    }

    @Test
    void registerPassOfficer_validRequest_encodesPasswordAndAssignsOfficerRole() {

        // Arrange
        RegisterUserDto request = registerRequest();

        UserResponseDto expectedResponse = userResponse(Role.PASS_OFFICER);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);

        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> {
            Users user = invocation.getArgument(0);
            user.setUserId(USER_ID);
            return user;
        });

        when(modelMapper.map(any(Users.class), org.mockito.ArgumentMatchers.eq(UserResponseDto.class))).thenReturn(expectedResponse);

        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);

        // Act
        UserResponseDto result = authService.registerPassOfficer(request);

        // Assert
        assertSame(expectedResponse, result);

        verify(passwordEncoder).encode(RAW_PASSWORD);

        verify(userRepository).save(userCaptor.capture());

        Users savedUser = userCaptor.getValue();

        assertEquals(USERNAME, savedUser.getUsername());

        assertEquals(ENCODED_PASSWORD, savedUser.getPassword());

        assertEquals(Role.PASS_OFFICER, savedUser.getRole());

        verify(modelMapper).map(savedUser, UserResponseDto.class);

        verifyNoInteractions(authenticationManager, jwtUtil);
    }


    // -------------------------------------------------------------------------
    // registerTicketChecker
    // -------------------------------------------------------------------------

    @Test
    void registerTicketChecker_usernameAlreadyExists_throwsBusinessException() {

        // Arrange
        RegisterUserDto request = registerRequest();

        Users existingUser = userWithRole(Role.TICKET_CHECKER);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(existingUser));

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> authService.registerTicketChecker(request));

        // Assert
        assertEquals("Username already exists", exception.getMessage());

        verifyNoInteractions(passwordEncoder, modelMapper, authenticationManager, jwtUtil);

        verify(userRepository, never()).save(any(Users.class));
    }

    @Test
    void registerTicketChecker_validRequest_encodesPasswordAndAssignsTicketCheckerRole() {

        // Arrange
        RegisterUserDto request = registerRequest();

        UserResponseDto expectedResponse = userResponse(Role.TICKET_CHECKER);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);

        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> {
            Users user = invocation.getArgument(0);
            user.setUserId(USER_ID);
            return user;
        });

        when(modelMapper.map(any(Users.class), org.mockito.ArgumentMatchers.eq(UserResponseDto.class))).thenReturn(expectedResponse);

        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);

        // Act
        UserResponseDto result = authService.registerTicketChecker(request);

        // Assert
        assertSame(expectedResponse, result);

        verify(passwordEncoder).encode(RAW_PASSWORD);

        verify(userRepository).save(userCaptor.capture());

        Users savedUser = userCaptor.getValue();

        assertEquals(USERNAME, savedUser.getUsername());

        assertEquals(ENCODED_PASSWORD, savedUser.getPassword());

        assertEquals(Role.TICKET_CHECKER, savedUser.getRole());

        verify(modelMapper).map(savedUser, UserResponseDto.class);

        verifyNoInteractions(authenticationManager, jwtUtil);
    }


    // -------------------------------------------------------------------------
    // login
    // -------------------------------------------------------------------------

    @Test
    void login_usernameNotFound_throwsResourceNotFound() {

        // Arrange
        LoginRequestDto request = loginRequest();

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> authService.login(request));

        // Assert
        assertEquals("Username not found while login", exception.getMessage());

        verifyNoInteractions(authenticationManager, jwtUtil, passwordEncoder, modelMapper);
    }

    @Test
    void login_validCredentials_authenticatesAndReturnsJwtResponse() {

        // Arrange
        LoginRequestDto request = loginRequest();

        Users user = userWithRole(Role.PASSENGER);

        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        when(jwtUtil.generateToken(USERNAME)).thenReturn(JWT_TOKEN);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authenticationCaptor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        // Act
        LoginResponseDto result = authService.login(request);

        // Assert
        assertEquals(JWT_TOKEN, result.getJwtToken());

        assertEquals(USERNAME, result.getUsername());

        assertEquals(Role.PASSENGER, result.getRole());

        verify(authenticationManager).authenticate(authenticationCaptor.capture());

        UsernamePasswordAuthenticationToken authenticationToken = authenticationCaptor.getValue();

        assertEquals(USERNAME, authenticationToken.getPrincipal());

        assertEquals(RAW_PASSWORD, authenticationToken.getCredentials());

        verify(jwtUtil).generateToken(USERNAME);

        verifyNoInteractions(passwordEncoder, modelMapper);
    }

    @Test
    void login_invalidCredentials_throwsBusinessExceptionAndDoesNotGenerateJwt() {

        // Arrange
        LoginRequestDto request = loginRequest();

        Users user = userWithRole(Role.PASSENGER);

        BadCredentialsException authenticationException = new BadCredentialsException("Bad credentials");

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(authenticationException);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(request));

        // Assert
        assertEquals("Error while login : " + authenticationException, exception.getMessage());

        verify(jwtUtil, never()).generateToken(any(String.class));

        verifyNoInteractions(passwordEncoder, modelMapper);
    }


    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private RegisterUserDto registerRequest() {

        return new RegisterUserDto(USERNAME, RAW_PASSWORD);
    }

    private LoginRequestDto loginRequest() {

        return new LoginRequestDto(USERNAME, RAW_PASSWORD);
    }

    private Users userWithRole(Role role) {

        return Users.builder().userId(USER_ID).username(USERNAME).password(ENCODED_PASSWORD).role(role).build();
    }

    private UserResponseDto userResponse(Role role) {

        return new UserResponseDto(USER_ID, USERNAME, role);
    }
}