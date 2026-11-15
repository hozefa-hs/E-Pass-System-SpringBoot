package com.porfolio.EPassSystemSpringboot.services;

import com.porfolio.EPassSystemSpringboot.dtos.PageResponse;
import com.porfolio.EPassSystemSpringboot.dtos.UserResponseDto;
import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.enums.Role;
import com.porfolio.EPassSystemSpringboot.exceptions.BusinessException;
import com.porfolio.EPassSystemSpringboot.exceptions.ResourceNotFoundException;
import com.porfolio.EPassSystemSpringboot.repositories.UserRepository;
import com.porfolio.EPassSystemSpringboot.services.implementations.UserServiceIMPL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final Long USER_ID = 10L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserServiceIMPL userService;


    // -------------------------------------------------------------------------
    // getAllUsers
    // -------------------------------------------------------------------------

    @Test
    void getAllUsers_negativePage_throwsBusinessException() {

        // Arrange

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> userService.getAllUsers(-1, 10));

        // Assert
        assertEquals("Page must not be negative", exception.getMessage());

        verifyNoInteractions(userRepository, modelMapper);
    }

    @Test
    void getAllUsers_zeroSize_throwsIllegalArgumentException() {

        // Arrange

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.getAllUsers(0, 0));

        // Assert
        assertEquals("Page size must not be less than one", exception.getMessage());

        verifyNoInteractions(userRepository, modelMapper);
    }

    @Test
    void getAllUsers_negativeSize_throwsIllegalArgumentException() {

        // Arrange

        // Act
        assertThrows(IllegalArgumentException.class, () -> userService.getAllUsers(0, -1));

        // Assert
        verifyNoInteractions(userRepository, modelMapper);
    }

    @Test
    void getAllUsers_sizeAboveFifty_capsPageSizeAtFifty() {

        // Arrange
        Page<Users> repositoryPage = new PageImpl<>(List.of(), org.springframework.data.domain.PageRequest.of(0, 50), 0);

        when(userRepository.findAll(org.mockito.ArgumentMatchers.any(Pageable.class))).thenReturn(repositoryPage);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        // Act
        PageResponse<UserResponseDto> result = userService.getAllUsers(0, 100);

        // Assert
        verify(userRepository).findAll(pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(0, pageable.getPageNumber());

        assertEquals(50, pageable.getPageSize());

        assertTrue(result.getContent().isEmpty());

        verifyNoInteractions(modelMapper);
    }

    @Test
    void getAllUsers_validPageAndSize_passesRequestedPaginationToRepository() {

        // Arrange
        Page<Users> repositoryPage = new PageImpl<>(List.of(), org.springframework.data.domain.PageRequest.of(2, 20), 0);

        when(userRepository.findAll(org.mockito.ArgumentMatchers.any(Pageable.class))).thenReturn(repositoryPage);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        // Act
        PageResponse<UserResponseDto> result = userService.getAllUsers(2, 20);

        // Assert
        verify(userRepository).findAll(pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(2, pageable.getPageNumber());

        assertEquals(20, pageable.getPageSize());

        assertTrue(result.getContent().isEmpty());

        verifyNoInteractions(modelMapper);
    }

    @Test
    void getAllUsers_noUsers_returnsEmptyPageResponse() {

        // Arrange
        Page<Users> repositoryPage = new PageImpl<>(List.of(), org.springframework.data.domain.PageRequest.of(0, 10), 0);

        when(userRepository.findAll(org.mockito.ArgumentMatchers.any(Pageable.class))).thenReturn(repositoryPage);

        // Act
        PageResponse<UserResponseDto> result = userService.getAllUsers(0, 10);

        // Assert
        assertTrue(result.getContent().isEmpty());

        assertEquals(0, result.getPage());

        assertEquals(10, result.getSize());

        assertEquals(0, result.getTotalElements());

        assertEquals(0, result.getTotalPages());

        assertTrue(result.isFirst());
        assertTrue(result.isLast());

        verifyNoInteractions(modelMapper);
    }

    @Test
    void getAllUsers_existingUsers_mapsUsersAndReturnsPaginationMetadata() {

        // Arrange
        Users passenger = userWithRole(1L, "passenger", Role.PASSENGER);

        Users officer = userWithRole(2L, "officer", Role.PASS_OFFICER);

        UserResponseDto passengerDto = new UserResponseDto(1L, "passenger", Role.PASSENGER);

        UserResponseDto officerDto = new UserResponseDto(2L, "officer", Role.PASS_OFFICER);

        Page<Users> repositoryPage = new PageImpl<>(List.of(passenger, officer), org.springframework.data.domain.PageRequest.of(1, 2), 5);

        when(userRepository.findAll(org.mockito.ArgumentMatchers.any(Pageable.class))).thenReturn(repositoryPage);

        when(modelMapper.map(passenger, UserResponseDto.class)).thenReturn(passengerDto);

        when(modelMapper.map(officer, UserResponseDto.class)).thenReturn(officerDto);

        // Act
        PageResponse<UserResponseDto> result = userService.getAllUsers(1, 2);

        // Assert
        assertEquals(2, result.getContent().size());

        assertSame(passengerDto, result.getContent().get(0));

        assertSame(officerDto, result.getContent().get(1));

        assertEquals(1, result.getPage());

        assertEquals(2, result.getSize());

        assertEquals(5, result.getTotalElements());

        assertEquals(3, result.getTotalPages());

        assertFalse(result.isFirst());
        assertFalse(result.isLast());

        verify(modelMapper).map(passenger, UserResponseDto.class);

        verify(modelMapper).map(officer, UserResponseDto.class);
    }


    // -------------------------------------------------------------------------
    // deleteUser
    // -------------------------------------------------------------------------

    @Test
    void deleteUser_userNotFound_throwsResourceNotFound() {

        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(USER_ID));

        // Assert
        assertEquals("User with id 10 not found", exception.getMessage());

        verify(userRepository, never()).deleteById(USER_ID);

        verifyNoInteractions(modelMapper);
    }

    @Test
    void deleteUser_adminAccount_throwsResourceNotFoundAndDoesNotDelete() {

        // Arrange
        Users admin = userWithRole(USER_ID, "admin", Role.ADMIN);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(admin));

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(USER_ID));

        // Assert
        assertEquals("Admin account cannot be deleted", exception.getMessage());

        verify(userRepository, never()).deleteById(USER_ID);

        verifyNoInteractions(modelMapper);
    }

    @Test
    void deleteUser_passenger_deletesUser() {

        // Arrange
        Users passenger = userWithRole(USER_ID, "passenger", Role.PASSENGER);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(passenger));

        // Act
        userService.deleteUser(USER_ID);

        // Assert
        verify(userRepository).deleteById(USER_ID);

        verifyNoInteractions(modelMapper);
    }

    @Test
    void deleteUser_passOfficer_deletesUser() {

        // Arrange
        Users officer = userWithRole(USER_ID, "officer", Role.PASS_OFFICER);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(officer));

        // Act
        userService.deleteUser(USER_ID);

        // Assert
        verify(userRepository).deleteById(USER_ID);

        verifyNoInteractions(modelMapper);
    }

    @Test
    void deleteUser_ticketChecker_deletesUser() {

        // Arrange
        Users ticketChecker = userWithRole(USER_ID, "checker", Role.TICKET_CHECKER);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(ticketChecker));

        // Act
        userService.deleteUser(USER_ID);

        // Assert
        verify(userRepository).deleteById(USER_ID);

        verifyNoInteractions(modelMapper);
    }

    @Test
    void deleteUser_nullRole_currentBehaviorDeletesUser() {

        // Arrange
        Users user = userWithRole(USER_ID, "user", null);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        // Act
        userService.deleteUser(USER_ID);

        // Assert
        verify(userRepository).deleteById(USER_ID);

        verifyNoInteractions(modelMapper);
    }


    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Users userWithRole(Long userId, String username, Role role) {

        return Users.builder().userId(userId).username(username).role(role).build();
    }
}