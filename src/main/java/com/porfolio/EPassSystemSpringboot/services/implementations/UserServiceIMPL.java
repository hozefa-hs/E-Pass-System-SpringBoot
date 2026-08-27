package com.porfolio.EPassSystemSpringboot.services.implementations;

import com.porfolio.EPassSystemSpringboot.dtos.RegisterUserDto;
import com.porfolio.EPassSystemSpringboot.dtos.UserResponseDto;
import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.enums.Role;
import com.porfolio.EPassSystemSpringboot.repositories.UserRepository;
import com.porfolio.EPassSystemSpringboot.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceIMPL implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;


    @Override
    public UserResponseDto registerPassenger(@Valid RegisterUserDto registerUserDto) {

        if (userRepository.findByUsername(registerUserDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        Users newUser = new Users();
        newUser.setUsername(registerUserDto.getUsername());
        newUser.setPassword(passwordEncoder.encode(registerUserDto.getPassword()));
        newUser.setRole(Role.PASSENGER);

        Users savedUser = userRepository.save(newUser);

        return modelMapper.map(savedUser, UserResponseDto.class);

    }

    @Override
    public UserResponseDto registerPassOfficer(RegisterUserDto registerUserDto) {

        if (userRepository.findByUsername(registerUserDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        Users newUser = new Users();
        newUser.setUsername(registerUserDto.getUsername());
        newUser.setPassword(passwordEncoder.encode(registerUserDto.getPassword()));
        newUser.setRole(Role.PASS_OFFICER);

        Users savedUser = userRepository.save(newUser);

        return modelMapper.map(savedUser, UserResponseDto.class);
    }

    @Override
    public UserResponseDto registerTicketChecker(RegisterUserDto registerUserDto) {

        if (userRepository.findByUsername(registerUserDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        Users newUser = new Users();
        newUser.setUsername(registerUserDto.getUsername());
        newUser.setPassword(passwordEncoder.encode(registerUserDto.getPassword()));
        newUser.setRole(Role.TICKET_CHECKER);

        Users savedUser = userRepository.save(newUser);

        return modelMapper.map(savedUser, UserResponseDto.class);
    }



    /*
    @Override
    public UserResponseDto registerAdmin(RegisterUserDto registerUserDto) {

        if (userRepository.findByUsername(registerUserDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        Users newUser = new Users();
        newUser.setUsername(registerUserDto.getUsername());
        newUser.setPassword(passwordEncoder.encode(registerUserDto.getPassword()));
        newUser.setRole(Role.ADMIN);

        Users savedUser = userRepository.save(newUser);

        return modelMapper.map(savedUser, UserResponseDto.class);
    }*/


}
