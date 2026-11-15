package com.porfolio.EPassSystemSpringboot.services;

import com.porfolio.EPassSystemSpringboot.dtos.CreatePassApplicationDto;
import com.porfolio.EPassSystemSpringboot.dtos.PassApplicationResponseDto;
import com.porfolio.EPassSystemSpringboot.dtos.ResubmitPassApplicationRequestDto;
import com.porfolio.EPassSystemSpringboot.entities.PassApplication;
import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.enums.*;
import com.porfolio.EPassSystemSpringboot.exceptions.BusinessException;
import com.porfolio.EPassSystemSpringboot.exceptions.ResourceNotFoundException;
import com.porfolio.EPassSystemSpringboot.repositories.PassApplicationRepository;
import com.porfolio.EPassSystemSpringboot.repositories.PassRepository;
import com.porfolio.EPassSystemSpringboot.repositories.UserRepository;
import com.porfolio.EPassSystemSpringboot.services.implementations.PassApplicationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PassApplicationServiceImplTest {

    private static final Long APPLICATION_ID = 10L;
    private static final Long USER_ID = 30L;

    private static final LocalDate FIXED_DATE = LocalDate.of(2026, 11, 14);

    @Mock
    private UserRepository userRepository;

    @Mock
    private PassRepository passRepository;

    @Mock
    private PassApplicationRepository passApplicationRepository;

    @Mock
    private ModelMapper modelMapper;

    @Spy
    private Clock businessClock = Clock.fixed(Instant.parse("2026-11-14T05:30:00Z"), ZoneId.of("Asia/Kolkata"));

    @InjectMocks
    private PassApplicationServiceImpl passApplicationService;


    // -------------------------------------------------------------------------
    // createApplication
    // -------------------------------------------------------------------------

    @Test
    void createApplication_userNotFound_throwsResourceNotFound() {

        // Arrange
        CreatePassApplicationDto request = sampleCreateDto();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> passApplicationService.createApplication(request, USER_ID));

        // Assert
        assertEquals("User not found while creating application", exception.getMessage());

        verifyNoInteractions(passRepository, passApplicationRepository, modelMapper);
    }

    @Test
    void createApplication_nonPassenger_throwsBusinessException() {

        // Arrange
        CreatePassApplicationDto request = sampleCreateDto();
        Users officer = userWithRole(Role.PASS_OFFICER);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(officer));

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> passApplicationService.createApplication(request, USER_ID));

        // Assert
        assertEquals("Only passengers can apply for pass", exception.getMessage());

        verifyNoInteractions(passRepository, passApplicationRepository, modelMapper);
    }

    @Test
    void createApplication_activePassExists_throwsBusinessException() {

        // Arrange
        CreatePassApplicationDto request = sampleCreateDto();
        Users passenger = userWithRole(Role.PASSENGER);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(passenger));

        when(passRepository.existsByUserAndPassStatusAndValidUntilGreaterThanEqual(passenger, PassStatus.ACTIVE, FIXED_DATE)).thenReturn(true);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> passApplicationService.createApplication(request, USER_ID));

        // Assert
        assertEquals("You already have an active pass", exception.getMessage());

        verify(passApplicationRepository, never()).existsByPassengerAndApplicationStatus(passenger, ApplicationStatus.PENDING);

        verify(passApplicationRepository, never()).save(any(PassApplication.class));

        verifyNoInteractions(modelMapper);
    }

    @Test
    void createApplication_pendingApplicationExists_throwsBusinessException() {

        // Arrange
        CreatePassApplicationDto request = sampleCreateDto();
        Users passenger = userWithRole(Role.PASSENGER);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(passenger));

        when(passApplicationRepository.existsByPassengerAndApplicationStatus(passenger, ApplicationStatus.PENDING)).thenReturn(true);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> passApplicationService.createApplication(request, USER_ID));

        // Assert
        assertEquals("You already have a pending application", exception.getMessage());

        verify(passRepository).existsByUserAndPassStatusAndValidUntilGreaterThanEqual(passenger, PassStatus.ACTIVE, FIXED_DATE);

        verify(passApplicationRepository, never()).save(any(PassApplication.class));

        verifyNoInteractions(modelMapper);
    }

    @Test
    void createApplication_validPassenger_createsPendingApplication() {

        // Arrange
        CreatePassApplicationDto request = sampleCreateDto();
        Users passenger = userWithRole(Role.PASSENGER);

        PassApplication mappedApplication = PassApplication.builder().passType(PassType.STUDENT).passValidity(PassValidity.ONE_MONTH).build();

        PassApplication savedApplication = PassApplication.builder().applicationId(APPLICATION_ID).applicationNumber("APP-12345678").passenger(passenger).passType(PassType.STUDENT).passValidity(PassValidity.ONE_MONTH).applicationStatus(ApplicationStatus.PENDING).appliedAt(LocalDateTime.now()).build();

        PassApplicationResponseDto expectedResponse = responseDto(APPLICATION_ID, ApplicationStatus.PENDING);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(passenger));

        when(modelMapper.map(request, PassApplication.class)).thenReturn(mappedApplication);

        when(passApplicationRepository.save(mappedApplication)).thenReturn(savedApplication);

        when(modelMapper.map(savedApplication, PassApplicationResponseDto.class)).thenReturn(expectedResponse);

        // Act
        PassApplicationResponseDto result = passApplicationService.createApplication(request, USER_ID);

        // Assert
        assertSame(expectedResponse, result);

        assertSame(passenger, mappedApplication.getPassenger());

        assertEquals(ApplicationStatus.PENDING, mappedApplication.getApplicationStatus());

        assertNotNull(mappedApplication.getAppliedAt());

        assertNotNull(mappedApplication.getApplicationNumber());

        assertTrue(mappedApplication.getApplicationNumber().matches("^APP-[A-F0-9]{8}$"));

        verify(passRepository).existsByUserAndPassStatusAndValidUntilGreaterThanEqual(passenger, PassStatus.ACTIVE, FIXED_DATE);

        verify(passApplicationRepository).existsByPassengerAndApplicationStatus(passenger, ApplicationStatus.PENDING);

        verify(passApplicationRepository).save(mappedApplication);

        verify(modelMapper).map(savedApplication, PassApplicationResponseDto.class);
    }


    // -------------------------------------------------------------------------
    // getOwnApplications
    // -------------------------------------------------------------------------

    @Test
    void getOwnApplications_noApplications_returnsEmptyList() {

        // Arrange
        when(passApplicationRepository.findAllByPassengerUserId(USER_ID)).thenReturn(List.of());

        // Act
        List<PassApplicationResponseDto> result = passApplicationService.getOwnApplications(USER_ID);

        // Assert
        assertTrue(result.isEmpty());

        verifyNoInteractions(modelMapper);
    }

    @Test
    void getOwnApplications_existingApplications_returnsMappedDtos() {

        // Arrange
        PassApplication firstApplication = application(10L, ApplicationStatus.PENDING);

        PassApplication secondApplication = application(11L, ApplicationStatus.REJECTED);

        PassApplicationResponseDto firstResponse = responseDto(10L, ApplicationStatus.PENDING);

        PassApplicationResponseDto secondResponse = responseDto(11L, ApplicationStatus.REJECTED);

        when(passApplicationRepository.findAllByPassengerUserId(USER_ID)).thenReturn(List.of(firstApplication, secondApplication));

        when(modelMapper.map(firstApplication, PassApplicationResponseDto.class)).thenReturn(firstResponse);

        when(modelMapper.map(secondApplication, PassApplicationResponseDto.class)).thenReturn(secondResponse);

        // Act
        List<PassApplicationResponseDto> result = passApplicationService.getOwnApplications(USER_ID);

        // Assert
        assertEquals(2, result.size());

        assertSame(firstResponse, result.get(0));

        assertSame(secondResponse, result.get(1));

        verify(modelMapper).map(firstApplication, PassApplicationResponseDto.class);

        verify(modelMapper).map(secondApplication, PassApplicationResponseDto.class);
    }


    // -------------------------------------------------------------------------
    // getPendingApplications
    // -------------------------------------------------------------------------

    @Test
    void getPendingApplications_negativePage_throwsBusinessException() {

        // Arrange

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> passApplicationService.getPendingApplications(-1, 10));

        // Assert
        assertEquals("Page must not be negative", exception.getMessage());

        verifyNoInteractions(passApplicationRepository, modelMapper);
    }

    @Test
    void getPendingApplications_zeroSize_throwsIllegalArgumentException() {

        // Arrange

        // Act
        assertThrows(IllegalArgumentException.class, () -> passApplicationService.getPendingApplications(0, 0));

        // Assert
        verifyNoInteractions(passApplicationRepository, modelMapper);
    }

    @Test
    void getPendingApplications_sizeAboveLimit_capsAtFifty() {

        // Arrange
        PageRequest expectedPageable = PageRequest.of(0, 50);

        Page<PassApplication> emptyPage = Page.empty(expectedPageable);

        when(passApplicationRepository.findAllByApplicationStatus(ApplicationStatus.PENDING, expectedPageable)).thenReturn(emptyPage);

        // Act
        Page<PassApplicationResponseDto> result = passApplicationService.getPendingApplications(0, 100);

        // Assert
        assertTrue(result.isEmpty());
        assertEquals(50, result.getSize());

        verify(passApplicationRepository).findAllByApplicationStatus(ApplicationStatus.PENDING, expectedPageable);

        verifyNoInteractions(modelMapper);
    }

    @Test
    void getPendingApplications_noPendingApplications_returnsEmptyPage() {

        // Arrange
        PageRequest pageable = PageRequest.of(1, 10);

        when(passApplicationRepository.findAllByApplicationStatus(ApplicationStatus.PENDING, pageable)).thenReturn(Page.empty(pageable));

        // Act
        Page<PassApplicationResponseDto> result = passApplicationService.getPendingApplications(1, 10);

        // Assert
        assertTrue(result.isEmpty());
        assertEquals(1, result.getNumber());
        assertEquals(10, result.getSize());

        verifyNoInteractions(modelMapper);
    }

    @Test
    void getPendingApplications_pendingApplications_returnsMappedPage() {

        // Arrange
        PageRequest pageable = PageRequest.of(0, 10);

        PassApplication application = application(APPLICATION_ID, ApplicationStatus.PENDING);

        Page<PassApplication> applicationPage = new PageImpl<>(List.of(application), pageable, 1);

        PassApplicationResponseDto expectedResponse = responseDto(APPLICATION_ID, ApplicationStatus.PENDING);

        when(passApplicationRepository.findAllByApplicationStatus(ApplicationStatus.PENDING, pageable)).thenReturn(applicationPage);

        when(modelMapper.map(application, PassApplicationResponseDto.class)).thenReturn(expectedResponse);

        // Act
        Page<PassApplicationResponseDto> result = passApplicationService.getPendingApplications(0, 10);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertSame(expectedResponse, result.getContent().get(0));

        verify(modelMapper).map(application, PassApplicationResponseDto.class);
    }


    // -------------------------------------------------------------------------
    // getApplicationById
    // -------------------------------------------------------------------------

    @Test
    void getApplicationById_userNotFound_throwsResourceNotFound() {

        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> passApplicationService.getApplicationById(APPLICATION_ID, USER_ID));

        // Assert
        assertEquals("User not found", exception.getMessage());

        verifyNoInteractions(passApplicationRepository, modelMapper);
    }

    @Test
    void passenger_canViewOwnApplication() {

        // Arrange
        Users passenger = userWithRole(Role.PASSENGER);

        PassApplication application = application(APPLICATION_ID, ApplicationStatus.PENDING);

        PassApplicationResponseDto expectedResponse = responseDto(APPLICATION_ID, ApplicationStatus.PENDING);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(passenger));

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        when(modelMapper.map(application, PassApplicationResponseDto.class)).thenReturn(expectedResponse);

        // Act
        PassApplicationResponseDto result = passApplicationService.getApplicationById(APPLICATION_ID, USER_ID);

        // Assert
        assertSame(expectedResponse, result);

        verify(passApplicationRepository, never()).findById(APPLICATION_ID);

        verify(modelMapper).map(application, PassApplicationResponseDto.class);
    }

    @Test
    void passenger_cannotViewSomeoneElsesApplication() {

        // Arrange
        Users passenger = userWithRole(Role.PASSENGER);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(passenger));

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(null);

        // Act
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> passApplicationService.getApplicationById(APPLICATION_ID, USER_ID));

        // Assert
        assertEquals("You are not authorized to view this application", exception.getMessage());

        verify(passApplicationRepository, never()).findById(APPLICATION_ID);

        verifyNoInteractions(modelMapper);
    }

    @Test
    void officer_canViewAnyApplication() {

        // Arrange
        Users officer = userWithRole(Role.PASS_OFFICER);

        PassApplication application = application(APPLICATION_ID, ApplicationStatus.PENDING);

        PassApplicationResponseDto expectedResponse = responseDto(APPLICATION_ID, ApplicationStatus.PENDING);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(officer));

        when(passApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        when(modelMapper.map(application, PassApplicationResponseDto.class)).thenReturn(expectedResponse);

        // Act
        PassApplicationResponseDto result = passApplicationService.getApplicationById(APPLICATION_ID, USER_ID);

        // Assert
        assertSame(expectedResponse, result);

        verify(passApplicationRepository, never()).findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID);

        verify(modelMapper).map(application, PassApplicationResponseDto.class);
    }

    @Test
    void officer_applicationNotFound_throwsResourceNotFound() {

        // Arrange
        Users officer = userWithRole(Role.PASS_OFFICER);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(officer));

        when(passApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> passApplicationService.getApplicationById(APPLICATION_ID, USER_ID));

        // Assert
        assertEquals("Pass application with id 10 not found", exception.getMessage());

        verifyNoInteractions(modelMapper);
    }

    @Test
    void admin_isDenied() {

        // Arrange
        Users admin = userWithRole(Role.ADMIN);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(admin));

        // Act
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> passApplicationService.getApplicationById(APPLICATION_ID, USER_ID));

        // Assert
        assertEquals("You are not authorized to view this application", exception.getMessage());

        verifyNoInteractions(passApplicationRepository, modelMapper);
    }

    @Test
    void ticketChecker_isDenied() {

        // Arrange
        Users ticketChecker = userWithRole(Role.TICKET_CHECKER);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(ticketChecker));

        // Act
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> passApplicationService.getApplicationById(APPLICATION_ID, USER_ID));

        // Assert
        assertEquals("You are not authorized to view this application", exception.getMessage());

        verifyNoInteractions(passApplicationRepository, modelMapper);
    }

    @Test
    void nullRole_isDenied() {

        // Arrange
        Users user = userWithRole(null);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        // Act
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> passApplicationService.getApplicationById(APPLICATION_ID, USER_ID));

        // Assert
        assertEquals("You are not authorized to view this application", exception.getMessage());

        verifyNoInteractions(passApplicationRepository, modelMapper);
    }


    // -------------------------------------------------------------------------
    // resubmitApplication
    // -------------------------------------------------------------------------

    @Test
    void resubmitApplication_applicationNotFound_throwsResourceNotFound() {

        // Arrange
        ResubmitPassApplicationRequestDto request = sampleResubmitDto();

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(null);

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> passApplicationService.resubmitApplication(APPLICATION_ID, USER_ID, request));

        // Assert
        assertEquals("Application not found", exception.getMessage());

        verify(passApplicationRepository, never()).save(any(PassApplication.class));

        verifyNoInteractions(modelMapper);
    }

    @Test
    void resubmitApplication_pendingApplication_throwsIllegalStateException() {

        // Arrange
        ResubmitPassApplicationRequestDto request = sampleResubmitDto();

        PassApplication application = application(APPLICATION_ID, ApplicationStatus.PENDING);

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> passApplicationService.resubmitApplication(APPLICATION_ID, USER_ID, request));

        // Assert
        assertEquals("Only rejected applications can be resubmitted", exception.getMessage());

        verify(passApplicationRepository, never()).save(application);

        verifyNoInteractions(modelMapper);
    }

    @Test
    void resubmitApplication_approvedApplication_throwsIllegalStateException() {

        // Arrange
        ResubmitPassApplicationRequestDto request = sampleResubmitDto();

        PassApplication application = application(APPLICATION_ID, ApplicationStatus.APPROVED);

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> passApplicationService.resubmitApplication(APPLICATION_ID, USER_ID, request));

        // Assert
        assertEquals("Only rejected applications can be resubmitted", exception.getMessage());

        verify(passApplicationRepository, never()).save(application);

        verifyNoInteractions(modelMapper);
    }

    @Test
    void resubmitApplication_rejectedApplication_resetsReviewStateAndReturnsPending() {

        // Arrange
        ResubmitPassApplicationRequestDto request = sampleResubmitDto();

        PassApplication application = application(APPLICATION_ID, ApplicationStatus.REJECTED);

        application.setPassType(PassType.STUDENT);
        application.setPassValidity(PassValidity.ONE_MONTH);
        application.setRejectionReason("Old rejection reason");
        application.setReviewedByOfficerId(99L);
        application.setReviewedAt(LocalDateTime.of(2026, 11, 1, 10, 0));

        PassApplicationResponseDto expectedResponse = responseDto(APPLICATION_ID, ApplicationStatus.PENDING);

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        when(passApplicationRepository.save(application)).thenReturn(application);

        when(modelMapper.map(application, PassApplicationResponseDto.class)).thenReturn(expectedResponse);

        // Act
        PassApplicationResponseDto result = passApplicationService.resubmitApplication(APPLICATION_ID, USER_ID, request);

        // Assert
        assertSame(expectedResponse, result);

        assertEquals(PassType.CORPORATE, application.getPassType());

        assertEquals(PassValidity.SIX_MONTH, application.getPassValidity());

        assertEquals(ApplicationStatus.PENDING, application.getApplicationStatus());

        assertNull(application.getReviewedAt());
        assertNull(application.getReviewedByOfficerId());
        assertNull(application.getRejectionReason());

        verify(passApplicationRepository).save(application);

        verify(modelMapper).map(application, PassApplicationResponseDto.class);
    }


    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Users userWithRole(Role role) {

        return Users.builder().userId(USER_ID).username("user").role(role).build();
    }

    private CreatePassApplicationDto sampleCreateDto() {

        return new CreatePassApplicationDto(PassType.STUDENT, PassValidity.ONE_MONTH);
    }

    private ResubmitPassApplicationRequestDto sampleResubmitDto() {

        return ResubmitPassApplicationRequestDto.builder().passType(PassType.CORPORATE).passValidity(PassValidity.SIX_MONTH).build();
    }

    private PassApplication application(Long applicationId, ApplicationStatus status) {

        return PassApplication.builder().applicationId(applicationId).applicationNumber("APP-12345678").passenger(userWithRole(Role.PASSENGER)).passType(PassType.STUDENT).passValidity(PassValidity.ONE_MONTH).applicationStatus(status).build();
    }

    private PassApplicationResponseDto responseDto(Long applicationId, ApplicationStatus status) {

        PassApplicationResponseDto response = new PassApplicationResponseDto();

        response.setApplicationId(applicationId);
        response.setApplicationNumber("APP-12345678");
        response.setPassengerId(USER_ID);
        response.setPassType(PassType.STUDENT);
        response.setPassValidity(PassValidity.ONE_MONTH);
        response.setApplicationStatus(status);

        return response;
    }
}