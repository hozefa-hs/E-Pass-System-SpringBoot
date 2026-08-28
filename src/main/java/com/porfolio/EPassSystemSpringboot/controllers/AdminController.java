package com.porfolio.EPassSystemSpringboot.controllers;

import com.porfolio.EPassSystemSpringboot.dtos.RegisterUserDto;
import com.porfolio.EPassSystemSpringboot.dtos.UserResponseDto;
import com.porfolio.EPassSystemSpringboot.services.AuthService;
import com.porfolio.EPassSystemSpringboot.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AuthService authService;
    private final UserService userService;

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

    @GetMapping("/all-users")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Page<UserResponseDto> allUsers = userService.getAllUsers(page, size);

        return ResponseEntity.ok(allUsers);
    }

    @DeleteMapping("/delete-user/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

}
