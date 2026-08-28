package com.porfolio.EPassSystemSpringboot.services;

import com.porfolio.EPassSystemSpringboot.dtos.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;


public interface UserService {

    Page<UserResponseDto> getAllUsers(int page, int size);

    void deleteUser(Long userId);

}
