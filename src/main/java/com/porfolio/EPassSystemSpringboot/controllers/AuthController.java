package com.porfolio.EPassSystemSpringboot.controllers;

import com.porfolio.EPassSystemSpringboot.dtos.LoginRequestDto;
import com.porfolio.EPassSystemSpringboot.dtos.LoginResponseDto;
import com.porfolio.EPassSystemSpringboot.dtos.RegisterUserDto;
import com.porfolio.EPassSystemSpringboot.dtos.UserResponseDto;
import com.porfolio.EPassSystemSpringboot.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto loginResponseDto = authService.login(loginRequestDto);
        return ResponseEntity.ok(loginResponseDto);
    }

    @PostMapping("/register-passenger")
    public ResponseEntity<UserResponseDto> registerPassenger(@Valid @RequestBody RegisterUserDto registerUserDto) {
        UserResponseDto response = authService.registerPassenger(registerUserDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /* This method is commented because Only One Admin exists in the system.
    @PostMapping("/register-admin")
    public ResponseEntity<UserResponseDto> registerAdmin(@Valid @RequestBody RegisterUserDto registerUserDto) {
        UserResponseDto response = userService.registerAdmin(registerUserDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    */


}
