package com.porfolio.EPassSystemSpringboot.controllers;

import com.porfolio.EPassSystemSpringboot.dtos.RegisterUserDto;
import com.porfolio.EPassSystemSpringboot.dtos.UserResponseDto;
import com.porfolio.EPassSystemSpringboot.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register-passenger")
    public ResponseEntity<UserResponseDto> registerPassenger(@RequestBody RegisterUserDto registerUserDto) {
        UserResponseDto response = userService.registerPassenger(registerUserDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    //@PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register-pass-officer")
    public ResponseEntity<UserResponseDto> registerPassOfficer(@RequestBody RegisterUserDto registerUserDto) {
        UserResponseDto response = userService.registerPassOfficer(registerUserDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    //@PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register-ticket-checker")
    public ResponseEntity<UserResponseDto> registerTicketChecker(@RequestBody RegisterUserDto registerUserDto) {
        UserResponseDto response = userService.registerTicketChecker(registerUserDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    /* This method is commented because Only One Admin exists in the system.
    @PostMapping("/register-admin")
    public ResponseEntity<UserResponseDto> registerAdmin(@RequestBody RegisterUserDto registerUserDto) {
        UserResponseDto response = userService.registerAdmin(registerUserDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    */


}
