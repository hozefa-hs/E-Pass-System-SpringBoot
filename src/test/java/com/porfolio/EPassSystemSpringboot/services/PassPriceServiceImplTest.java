package com.porfolio.EPassSystemSpringboot.services;


import com.porfolio.EPassSystemSpringboot.entities.PassPrice;
import com.porfolio.EPassSystemSpringboot.enums.PassType;
import com.porfolio.EPassSystemSpringboot.enums.PassValidity;
import com.porfolio.EPassSystemSpringboot.exceptions.BusinessException;
import com.porfolio.EPassSystemSpringboot.repositories.PassPriceRepository;
import com.porfolio.EPassSystemSpringboot.services.implementations.PassPriceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PassPriceServiceImplTest {

    @Mock
    private PassPriceRepository passPriceRepository;

    @InjectMocks
    private PassPriceServiceImpl passPriceService;


    @Test
    void getPrice_priceConfigured_returnsConfiguredAmount() {

        // Arrange
        PassType passType = PassType.STUDENT;

        PassValidity passValidity = PassValidity.THREE_MONTH;

        BigDecimal configuredAmount = new BigDecimal("450.00");

        PassPrice passPrice = PassPrice.builder().priceId(1L).passType(passType).passValidity(passValidity).amount(configuredAmount).build();

        when(passPriceRepository.findByPassTypeAndPassValidity(passType, passValidity)).thenReturn(Optional.of(passPrice));

        // Act
        BigDecimal result = passPriceService.getPrice(passType, passValidity);

        // Assert
        assertSame(configuredAmount, result);

        assertEquals(new BigDecimal("450.00"), result);

        verify(passPriceRepository).findByPassTypeAndPassValidity(PassType.STUDENT, PassValidity.THREE_MONTH);
    }

    @Test
    void getPrice_priceNotConfigured_throwsBusinessException() {

        // Arrange
        PassType passType = PassType.CORPORATE;

        PassValidity passValidity = PassValidity.SIX_MONTH;

        when(passPriceRepository.findByPassTypeAndPassValidity(passType, passValidity)).thenReturn(Optional.empty());

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> passPriceService.getPrice(passType, passValidity));

        // Assert
        assertEquals("Price not configured for CORPORATE - SIX_MONTH", exception.getMessage());

        verify(passPriceRepository).findByPassTypeAndPassValidity(PassType.CORPORATE, PassValidity.SIX_MONTH);
    }

    @Test
    void getPrice_nullPassType_currentBehaviorQueriesRepositoryAndThrowsBusinessException() {

        // Arrange
        PassValidity passValidity = PassValidity.ONE_MONTH;

        when(passPriceRepository.findByPassTypeAndPassValidity(null, passValidity)).thenReturn(Optional.empty());

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> passPriceService.getPrice(null, passValidity));

        // Assert
        assertEquals("Price not configured for null - ONE_MONTH", exception.getMessage());

        verify(passPriceRepository).findByPassTypeAndPassValidity(null, PassValidity.ONE_MONTH);
    }

    @Test
    void getPrice_nullPassValidity_currentBehaviorQueriesRepositoryAndThrowsBusinessException() {

        // Arrange
        PassType passType = PassType.SENIOR_CITIZEN;

        when(passPriceRepository.findByPassTypeAndPassValidity(passType, null)).thenReturn(Optional.empty());

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> passPriceService.getPrice(passType, null));

        // Assert
        assertEquals("Price not configured for SENIOR_CITIZEN - null", exception.getMessage());

        verify(passPriceRepository).findByPassTypeAndPassValidity(PassType.SENIOR_CITIZEN, null);
    }

    @Test
    void getPrice_bothArgumentsNull_currentBehaviorQueriesRepositoryAndThrowsBusinessException() {

        // Arrange
        when(passPriceRepository.findByPassTypeAndPassValidity(null, null)).thenReturn(Optional.empty());

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> passPriceService.getPrice(null, null));

        // Assert
        assertEquals("Price not configured for null - null", exception.getMessage());

        verify(passPriceRepository).findByPassTypeAndPassValidity(null, null);
    }
}