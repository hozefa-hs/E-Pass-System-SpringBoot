package com.porfolio.EPassSystemSpringboot.services.implementations;

import com.porfolio.EPassSystemSpringboot.dtos.UserResponseDto;
import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.enums.Role;
import com.porfolio.EPassSystemSpringboot.exceptions.ResourceNotFoundException;
import com.porfolio.EPassSystemSpringboot.repositories.UserRepository;
import com.porfolio.EPassSystemSpringboot.services.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceIMPL implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<UserResponseDto> getAllUsers(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Users> usersList = userRepository.findAll(pageable);

        return usersList
                .map(users -> modelMapper.map(users, UserResponseDto.class));
    }

    @Override
    public void deleteUser(Long userId) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));
        if(user.getRole() == Role.ADMIN) {
            throw new ResourceNotFoundException("Admin account cannot be deleted");
        }
        userRepository.deleteById(userId);
    }
}
