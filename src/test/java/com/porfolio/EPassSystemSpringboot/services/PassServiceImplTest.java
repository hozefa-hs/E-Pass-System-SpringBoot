package com.porfolio.EPassSystemSpringboot.services;


import com.porfolio.EPassSystemSpringboot.dtos.PassResponseDto;
import com.porfolio.EPassSystemSpringboot.dtos.PassValidationResponseDto;
import com.porfolio.EPassSystemSpringboot.entities.Pass;
import com.porfolio.EPassSystemSpringboot.entities.PassApplication;
import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.enums.*;
import com.porfolio.EPassSystemSpringboot.exceptions.BusinessException;
import com.porfolio.EPassSystemSpringboot.exceptions.ResourceNotFoundException;
import com.porfolio.EPassSystemSpringboot.repositories.PassApplicationRepository;
import com.porfolio.EPassSystemSpringboot.repositories.PassRepository;
import com.porfolio.EPassSystemSpringboot.services.implementations.PassServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PassServiceImplTest {

    private static final Long APPLICATION_ID = 10L;
    private static final Long USER_ID = 20L;
    private static final Long PASS_ID = 30L;

    private static final String QR_TOKEN = "9c6d2311-102c-4a72-b5c2-123456789abc";

    private static final LocalDate FIXED_DATE = LocalDate.of(2026, 11, 14);

    @Mock
    private PassApplicationRepository passApplicationRepository;

    @Mock
    private PassRepository passRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private QrCodeService qrCodeService;

    @Spy
    private Clock businessClock = Clock.fixed(Instant.parse("2026-11-14T05:30:00Z"), ZoneId.of("Asia/Kolkata"));

    @InjectMocks
    private PassServiceImpl passService;


    // -------------------------------------------------------------------------
    // issuePass
    // -------------------------------------------------------------------------

    @Test
    void issuePass_applicationNotFound_throwsResourceNotFound() {

        // Arrange
        when(passApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> passService.issuePass(APPLICATION_ID));

        // Assert
        assertEquals("Pass application not found", exception.getMessage());

        verifyNoInteractions(passRepository, modelMapper, qrCodeService);
    }

    @Test
    void issuePass_applicationNotApproved_throwsBusinessException() {

        // Arrange
        PassApplication application = applicationWithStatus(ApplicationStatus.PENDING);

        when(passApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> passService.issuePass(APPLICATION_ID));

        // Assert
        assertEquals("Pass can only be issued for an approved application", exception.getMessage());

        verifyNoInteractions(passRepository, modelMapper, qrCodeService);
    }

    @Test
    void issuePass_passAlreadyExistsForApplication_throwsBusinessException() {

        // Arrange
        PassApplication application = applicationWithStatus(ApplicationStatus.APPROVED);

        when(passApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        when(passRepository.existsByPassApplication(application)).thenReturn(true);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> passService.issuePass(APPLICATION_ID));

        // Assert
        assertEquals("Pass already exists for application id : 10", exception.getMessage());

        verify(passRepository, never()).existsByUserAndPassStatusAndValidUntilGreaterThanEqual(any(Users.class), eq(PassStatus.ACTIVE), any(LocalDate.class));

        verify(passRepository, never()).save(any(Pass.class));

        verifyNoInteractions(modelMapper, qrCodeService);
    }

    @Test
    void issuePass_passengerAlreadyHasActivePass_throwsBusinessException() {

        // Arrange
        PassApplication application = applicationWithStatus(ApplicationStatus.APPROVED);

        when(passApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        when(passRepository.existsByUserAndPassStatusAndValidUntilGreaterThanEqual(application.getPassenger(), PassStatus.ACTIVE, FIXED_DATE)).thenReturn(true);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> passService.issuePass(APPLICATION_ID));

        // Assert
        assertEquals("Passenger already has an active pass", exception.getMessage());

        verify(passRepository).existsByPassApplication(application);

        verify(passRepository, never()).save(any(Pass.class));

        verifyNoInteractions(modelMapper, qrCodeService);
    }

    @Test
    void issuePass_approvedApplication_createsActivePassWithCorrectValidityDates() {

        // Arrange
        PassApplication application = applicationWithStatus(ApplicationStatus.APPROVED);

        PassResponseDto expectedResponse = PassResponseDto.builder().passId(PASS_ID).applicationId(APPLICATION_ID).passengerId(USER_ID).passType(PassType.STUDENT).passValidity(PassValidity.ONE_MONTH).passStatus(PassStatus.ACTIVE).validFrom(FIXED_DATE).validUntil(LocalDate.of(2026, 12, 13)).build();

        when(passApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        when(passRepository.save(any(Pass.class))).thenAnswer(invocation -> {
            Pass pass = invocation.getArgument(0);
            pass.setPassId(PASS_ID);
            return pass;
        });

        when(modelMapper.map(any(Pass.class), eq(PassResponseDto.class))).thenReturn(expectedResponse);

        ArgumentCaptor<Pass> passCaptor = ArgumentCaptor.forClass(Pass.class);

        // Act
        PassResponseDto result = passService.issuePass(APPLICATION_ID);

        // Assert
        assertSame(expectedResponse, result);

        verify(passRepository).existsByPassApplication(application);

        verify(passRepository).existsByUserAndPassStatusAndValidUntilGreaterThanEqual(application.getPassenger(), PassStatus.ACTIVE, FIXED_DATE);

        verify(passRepository).save(passCaptor.capture());

        Pass createdPass = passCaptor.getValue();

        assertEquals(application, createdPass.getPassApplication());

        assertSame(application.getPassenger(), createdPass.getUser());

        assertEquals(PassType.STUDENT, createdPass.getPassType());

        assertEquals(PassValidity.ONE_MONTH, createdPass.getPassValidity());

        assertEquals(PassStatus.ACTIVE, createdPass.getPassStatus());

        assertEquals(FIXED_DATE, createdPass.getValidFrom());

        assertEquals(LocalDate.of(2026, 12, 13), createdPass.getValidUntil());

        assertNotNull(createdPass.getPassNumber());

        assertTrue(createdPass.getPassNumber().matches("^PASS-[A-F0-9]{8}$"));

        assertNotNull(createdPass.getQrToken());

        assertFalse(createdPass.getQrToken().isBlank());

        verify(modelMapper).map(createdPass, PassResponseDto.class);

        verifyNoInteractions(qrCodeService);
    }


    // -------------------------------------------------------------------------
    // validatePass
    // -------------------------------------------------------------------------

    @Test
    void validatePass_tokenNotFound_throwsResourceNotFound() {

        // Arrange
        when(passRepository.findByQrToken(QR_TOKEN)).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> passService.validatePass(QR_TOKEN));

        // Assert
        assertEquals("Pass not found", exception.getMessage());

        verifyNoInteractions(passApplicationRepository, modelMapper, qrCodeService);
    }

    @Test
    void validatePass_beforeValidFrom_returnsInvalidNotYetValid() {

        // Arrange
        Pass pass = pass(PassStatus.ACTIVE, FIXED_DATE.plusDays(1), FIXED_DATE.plusMonths(1));

        when(passRepository.findByQrToken(QR_TOKEN)).thenReturn(Optional.of(pass));

        // Act
        PassValidationResponseDto result = passService.validatePass(QR_TOKEN);

        // Assert
        assertFalse(result.isValid());

        assertEquals("Pass is not yet valid.", result.getMessage());
    }

    @Test
    void validatePass_afterValidUntil_returnsExpired() {

        // Arrange
        Pass pass = pass(PassStatus.ACTIVE, FIXED_DATE.minusMonths(1), FIXED_DATE.minusDays(1));

        when(passRepository.findByQrToken(QR_TOKEN)).thenReturn(Optional.of(pass));

        // Act
        PassValidationResponseDto result = passService.validatePass(QR_TOKEN);

        // Assert
        assertFalse(result.isValid());

        assertEquals("Pass has expired.", result.getMessage());
    }

    @Test
    void validatePass_inDateRangeButExpiredStatus_returnsInactive() {

        // Arrange
        Pass pass = pass(PassStatus.EXPIRED, FIXED_DATE.minusDays(5), FIXED_DATE.plusDays(5));

        when(passRepository.findByQrToken(QR_TOKEN)).thenReturn(Optional.of(pass));

        // Act
        PassValidationResponseDto result = passService.validatePass(QR_TOKEN);

        // Assert
        assertFalse(result.isValid());

        assertEquals("Pass is not active.", result.getMessage());

        assertEquals(PassStatus.EXPIRED, result.getPassStatus());
    }

    @Test
    void validatePass_onValidFrom_isValid() {

        // Arrange
        Pass pass = pass(PassStatus.ACTIVE, FIXED_DATE, FIXED_DATE.plusMonths(1));

        when(passRepository.findByQrToken(QR_TOKEN)).thenReturn(Optional.of(pass));

        // Act
        PassValidationResponseDto result = passService.validatePass(QR_TOKEN);

        // Assert
        assertTrue(result.isValid());

        assertEquals("Pass is valid.", result.getMessage());
    }

    @Test
    void validatePass_onValidUntil_isValid() {

        // Arrange
        Pass pass = pass(PassStatus.ACTIVE, FIXED_DATE.minusMonths(1), FIXED_DATE);

        when(passRepository.findByQrToken(QR_TOKEN)).thenReturn(Optional.of(pass));

        // Act
        PassValidationResponseDto result = passService.validatePass(QR_TOKEN);

        // Assert
        assertTrue(result.isValid());

        assertEquals("Pass is valid.", result.getMessage());
    }

    @Test
    void validatePass_activeAndWithinDates_returnsCompleteValidationResponse() {

        // Arrange
        Pass pass = pass(PassStatus.ACTIVE, FIXED_DATE.minusDays(5), FIXED_DATE.plusDays(20));

        when(passRepository.findByQrToken(QR_TOKEN)).thenReturn(Optional.of(pass));

        // Act
        PassValidationResponseDto result = passService.validatePass(QR_TOKEN);

        // Assert
        assertTrue(result.isValid());

        assertEquals("Pass is valid.", result.getMessage());

        assertEquals("PASS-12345678", result.getPassNumber());

        assertEquals("passenger", result.getPassengerUsername());

        assertEquals(PassType.STUDENT, result.getPassType());

        assertEquals(PassValidity.ONE_MONTH, result.getPassValidity());

        assertEquals(PassStatus.ACTIVE, result.getPassStatus());

        assertEquals(FIXED_DATE.minusDays(5), result.getValidFrom());

        assertEquals(FIXED_DATE.plusDays(20), result.getValidUntil());
    }


    // -------------------------------------------------------------------------
    // generateActivePassQrCode
    // -------------------------------------------------------------------------

    @Test
    void generateActivePassQrCode_noCurrentActivePass_throwsResourceNotFound() {

        // Arrange
        when(passRepository.findByUser_UserIdAndPassStatusAndValidFromLessThanEqualAndValidUntilGreaterThanEqual(USER_ID, PassStatus.ACTIVE, FIXED_DATE, FIXED_DATE)).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> passService.generateActivePassQrCode(USER_ID));

        // Assert
        assertEquals("No currently valid active pass found for this passenger", exception.getMessage());

        verifyNoInteractions(qrCodeService);
    }

    @Test
    void generateActivePassQrCode_activePass_generatesQrFromToken() {

        // Arrange
        Pass pass = pass(PassStatus.ACTIVE, FIXED_DATE.minusDays(5), FIXED_DATE.plusDays(20));

        byte[] expectedQrBytes = new byte[]{1, 2, 3, 4, 5};

        when(passRepository.findByUser_UserIdAndPassStatusAndValidFromLessThanEqualAndValidUntilGreaterThanEqual(USER_ID, PassStatus.ACTIVE, FIXED_DATE, FIXED_DATE)).thenReturn(Optional.of(pass));

        when(qrCodeService.generateQrCode(QR_TOKEN, 300, 300)).thenReturn(expectedQrBytes);

        // Act
        byte[] result = passService.generateActivePassQrCode(USER_ID);

        // Assert
        assertArrayEquals(expectedQrBytes, result);

        verify(qrCodeService).generateQrCode(QR_TOKEN, 300, 300);
    }


    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Users passenger() {

        return Users.builder().userId(USER_ID).username("passenger").role(Role.PASSENGER).build();
    }

    private PassApplication applicationWithStatus(ApplicationStatus status) {

        return PassApplication.builder().applicationId(APPLICATION_ID).applicationNumber("APP-12345678").passenger(passenger()).passType(PassType.STUDENT).passValidity(PassValidity.ONE_MONTH).applicationStatus(status).build();
    }

    private Pass pass(PassStatus status, LocalDate validFrom, LocalDate validUntil) {

        return Pass.builder().passId(PASS_ID).passNumber("PASS-12345678").qrToken(QR_TOKEN).user(passenger()).passApplication(applicationWithStatus(ApplicationStatus.APPROVED)).passType(PassType.STUDENT).passValidity(PassValidity.ONE_MONTH).passStatus(status).validFrom(validFrom).validUntil(validUntil).build();
    }
}
