package com.porfolio.EPassSystemSpringboot.services;


import com.porfolio.EPassSystemSpringboot.enums.PassStatus;
import com.porfolio.EPassSystemSpringboot.repositories.PassRepository;
import com.porfolio.EPassSystemSpringboot.services.implementations.PassExpirationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PassExpirationServiceImplTest {

    @Mock
    private PassRepository passRepository;

    @InjectMocks
    private PassExpirationServiceImpl passExpirationService;


    @Test
    void expireExpiredPasses_expiredPassesExist_returnsNumberOfUpdatedPasses() {

        // Arrange
        LocalDate today = LocalDate.of(2026, 11, 14);

        when(passRepository.expireActivePasses(PassStatus.ACTIVE, PassStatus.EXPIRED, today)).thenReturn(4);

        // Act
        int result = passExpirationService.expireExpiredPasses(today);

        // Assert
        assertEquals(4, result);

        verify(passRepository).expireActivePasses(PassStatus.ACTIVE, PassStatus.EXPIRED, today);
    }

    @Test
    void expireExpiredPasses_noExpiredPasses_returnsZero() {

        // Arrange
        LocalDate today = LocalDate.of(2026, 11, 14);

        when(passRepository.expireActivePasses(PassStatus.ACTIVE, PassStatus.EXPIRED, today)).thenReturn(0);

        // Act
        int result = passExpirationService.expireExpiredPasses(today);

        // Assert
        assertEquals(0, result);

        verify(passRepository).expireActivePasses(PassStatus.ACTIVE, PassStatus.EXPIRED, today);
    }

    @Test
    void expireExpiredPasses_givenDate_passesExactDateToRepository() {

        // Arrange
        LocalDate requestedDate = LocalDate.of(2030, 3, 25);

        when(passRepository.expireActivePasses(PassStatus.ACTIVE, PassStatus.EXPIRED, requestedDate)).thenReturn(2);

        // Act
        int result = passExpirationService.expireExpiredPasses(requestedDate);

        // Assert
        assertEquals(2, result);

        verify(passRepository).expireActivePasses(PassStatus.ACTIVE, PassStatus.EXPIRED, LocalDate.of(2030, 3, 25));
    }

    @Test
    void expireExpiredPasses_nullDate_currentBehaviorDelegatesNullToRepository() {

        // Arrange
        when(passRepository.expireActivePasses(PassStatus.ACTIVE, PassStatus.EXPIRED, null)).thenReturn(0);

        // Act
        int result = passExpirationService.expireExpiredPasses(null);

        // Assert
        assertEquals(0, result);

        verify(passRepository).expireActivePasses(PassStatus.ACTIVE, PassStatus.EXPIRED, null);
    }
}