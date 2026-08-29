package com.porfolio.EPassSystemSpringboot.services;

import com.porfolio.EPassSystemSpringboot.dtos.UserResponseDto;
import org.springframework.data.domain.Page;

public interface UserService {

    Page<UserResponseDto> getAllUsers(int page, int size);

    void deleteUser(Long userId);

}
