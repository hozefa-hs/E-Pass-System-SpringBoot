package com.porfolio.EPassSystemSpringboot.services;

import com.porfolio.EPassSystemSpringboot.dtos.RegisterUserDto;
import com.porfolio.EPassSystemSpringboot.dtos.UserResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    UserResponseDto registerPassenger(RegisterUserDto registerUserDto);

    UserResponseDto registerPassOfficer(RegisterUserDto registerUserDto);

    UserResponseDto registerTicketChecker(RegisterUserDto registerUserDto);

    //UserResponseDto registerAdmin(RegisterUserDto registerUserDto);
}
