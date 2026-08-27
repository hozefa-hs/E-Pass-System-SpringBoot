package com.porfolio.EPassSystemSpringboot.services;

import com.porfolio.EPassSystemSpringboot.dtos.LoginRequestDto;
import com.porfolio.EPassSystemSpringboot.dtos.LoginResponseDto;
import com.porfolio.EPassSystemSpringboot.dtos.RegisterUserDto;
import com.porfolio.EPassSystemSpringboot.dtos.UserResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {

    UserResponseDto registerPassenger(RegisterUserDto registerUserDto);

    UserResponseDto registerPassOfficer(RegisterUserDto registerUserDto);

    UserResponseDto registerTicketChecker(RegisterUserDto registerUserDto);

    LoginResponseDto login(LoginRequestDto loginRequestDto);

    //UserResponseDto registerAdmin(RegisterUserDto registerUserDto);

}
