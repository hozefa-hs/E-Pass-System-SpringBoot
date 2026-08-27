package com.porfolio.EPassSystemSpringboot.controllers;

import com.porfolio.EPassSystemSpringboot.dtos.RegisterUserDto;
import com.porfolio.EPassSystemSpringboot.dtos.UserResponseDto;
import com.porfolio.EPassSystemSpringboot.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AuthService authService;

    @PostMapping("/register-pass-officer")
    public ResponseEntity<UserResponseDto> registerPassOfficer(@Valid @RequestBody RegisterUserDto registerUserDto) {
        UserResponseDto response = authService.registerPassOfficer(registerUserDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/register-ticket-checker")
    public ResponseEntity<UserResponseDto> registerTicketChecker(@Valid @RequestBody RegisterUserDto registerUserDto) {
        UserResponseDto response = authService.registerTicketChecker(registerUserDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
