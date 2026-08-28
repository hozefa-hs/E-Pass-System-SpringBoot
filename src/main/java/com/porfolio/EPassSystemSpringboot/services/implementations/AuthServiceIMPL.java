package com.porfolio.EPassSystemSpringboot.services.implementations;

import com.porfolio.EPassSystemSpringboot.dtos.LoginRequestDto;
import com.porfolio.EPassSystemSpringboot.dtos.LoginResponseDto;
import com.porfolio.EPassSystemSpringboot.dtos.RegisterUserDto;
import com.porfolio.EPassSystemSpringboot.dtos.UserResponseDto;
import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.enums.Role;
import com.porfolio.EPassSystemSpringboot.exceptions.ResourceNotFoundException;
import com.porfolio.EPassSystemSpringboot.repositories.UserRepository;
import com.porfolio.EPassSystemSpringboot.services.AuthService;
import com.porfolio.EPassSystemSpringboot.utils.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceIMPL implements AuthService {


    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;


    @Override
    public UserResponseDto registerPassenger(RegisterUserDto registerUserDto) {

        if (userRepository.findByUsername(registerUserDto.getUsername()).isPresent()) {
            throw new ResourceNotFoundException("Username already exists");
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
            throw new ResourceNotFoundException("Username already exists");
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
            throw new ResourceNotFoundException("Username already exists");
        }

        Users newUser = new Users();
        newUser.setUsername(registerUserDto.getUsername());
        newUser.setPassword(passwordEncoder.encode(registerUserDto.getPassword()));
        newUser.setRole(Role.TICKET_CHECKER);

        Users savedUser = userRepository.save(newUser);

        return modelMapper.map(savedUser, UserResponseDto.class);
    }

    @Override
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {

        Users user = userRepository.findByUsername(loginRequestDto.getUsername()).orElseThrow(() -> new ResourceNotFoundException("Username not found while login"));

        try {
            //first authenticate the user
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword()));

            //generate Jwt Token
            String jwtToken = jwtUtil.generateToken(loginRequestDto.getUsername());

            return new LoginResponseDto(jwtToken, loginRequestDto.getUsername(), user.getRole());

        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Error while login : "+e);
        }
    }



    /*
    @Override
    public UserResponseDto registerAdmin(RegisterUserDto registerUserDto) {

        if (userRepository.findByUsername(registerUserDto.getUsername()).isPresent()) {
            throw new ResourceNotFoundException("Username already exists");
        }

        Users newUser = new Users();
        newUser.setUsername(registerUserDto.getUsername());
        newUser.setPassword(passwordEncoder.encode(registerUserDto.getPassword()));
        newUser.setRole(Role.ADMIN);

        Users savedUser = userRepository.save(newUser);

        return modelMapper.map(savedUser, UserResponseDto.class);
    }*/


}
