package com.porfolio.EPassSystemSpringboot.services;

import com.porfolio.EPassSystemSpringboot.dtos.PaymentInitiationResponseDto;
import com.porfolio.EPassSystemSpringboot.dtos.PaymentResponseDto;
import com.porfolio.EPassSystemSpringboot.entities.PassApplication;
import com.porfolio.EPassSystemSpringboot.entities.Payment;
import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.enums.*;
import com.porfolio.EPassSystemSpringboot.exceptions.BusinessException;
import com.porfolio.EPassSystemSpringboot.exceptions.ResourceNotFoundException;
import com.porfolio.EPassSystemSpringboot.repositories.PassApplicationRepository;
import com.porfolio.EPassSystemSpringboot.repositories.PassRepository;
import com.porfolio.EPassSystemSpringboot.repositories.PaymentRepository;
import com.porfolio.EPassSystemSpringboot.services.implementations.PaymentServiceImpl;
import com.razorpay.RazorpayException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    private static final Long APPLICATION_ID = 10L;
    private static final Long PAYMENT_ID = 20L;
    private static final Long USER_ID = 30L;

    private static final String APPLICATION_NUMBER = "APP-12345678";
    private static final String RAZORPAY_ORDER_ID = "order_123";
    private static final String RAZORPAY_PAYMENT_ID = "pay_123";
    private static final String RAZORPAY_SIGNATURE = "signature_123";
    private static final String RAZORPAY_KEY_ID = "rzp_test_key";
    private static final String PAYMENT_METHOD = "upi";

    private static final BigDecimal AMOUNT = new BigDecimal("1000.00");
    private static final long AMOUNT_IN_PAISE = 100000L;

    private static final LocalDate FIXED_DATE = LocalDate.of(2026, 11, 12);


    @Mock private ModelMapper modelMapper;
    @Mock private PassApplicationRepository passApplicationRepository;
    @Mock private PassRepository passRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PassPriceService passPriceService;
    @Mock private PassService passService;
    @Mock private RazorpayService razorpayService;

    @Spy
    private Clock businessClock = Clock.fixed(Instant.parse("2026-11-12T06:30:00Z"), ZoneId.of("Asia/Kolkata"));

    @InjectMocks
    private PaymentServiceImpl paymentService;


    // -------------------------------------------------------------------------
    // initiatePayment
    // -------------------------------------------------------------------------

    @Test
    void initiatePayment_applicationNotFound_throwsResourceNotFound() throws RazorpayException {

        // Arrange
        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(null);

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> paymentService.initiatePayment(APPLICATION_ID, USER_ID));

        // Assert
        assertEquals("Pass application not found", exception.getMessage());

        verifyNoInteractions(passRepository, paymentRepository, passPriceService, razorpayService, passService, modelMapper);
    }

    @Test
    void initiatePayment_applicationNotApproved_throwsBusinessException() throws RazorpayException {

        // Arrange
        PassApplication application = applicationWithStatus(ApplicationStatus.PENDING);

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.initiatePayment(APPLICATION_ID, USER_ID));

        // Assert
        assertEquals("Payment can only be initiated for an approved application", exception.getMessage());

        verifyNoInteractions(passRepository, paymentRepository, passPriceService, razorpayService, passService, modelMapper);
    }

    @Test
    void initiatePayment_activePassExists_throwsBusinessException() throws RazorpayException {

        // Arrange
        PassApplication application = applicationWithStatus(ApplicationStatus.APPROVED);

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        when(passRepository.existsByUserAndPassStatusAndValidUntilGreaterThanEqual(application.getPassenger(), PassStatus.ACTIVE, FIXED_DATE)).thenReturn(true);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.initiatePayment(APPLICATION_ID, USER_ID));

        // Assert
        assertEquals("Passenger already has an active pass", exception.getMessage());

        verifyNoInteractions(passPriceService, razorpayService, passService, modelMapper);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void initiatePayment_successfulPaymentAlreadyExists_throwsBusinessException() throws RazorpayException {

        // Arrange
        PassApplication application = applicationWithStatus(ApplicationStatus.APPROVED);

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        when(paymentRepository.existsByPassApplicationApplicationIdAndPaymentStatus(APPLICATION_ID, PaymentStatus.SUCCESS)).thenReturn(true);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.initiatePayment(APPLICATION_ID, USER_ID));

        // Assert
        assertEquals("Payment has already been completed for this application", exception.getMessage());

        verify(passRepository).existsByUserAndPassStatusAndValidUntilGreaterThanEqual(application.getPassenger(), PassStatus.ACTIVE, FIXED_DATE);

        verifyNoInteractions(passPriceService, razorpayService, passService, modelMapper);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void initiatePayment_validApplication_createsPendingPaymentAndRazorpayOrder() throws RazorpayException {

        // Arrange
        PassApplication application = applicationWithStatus(ApplicationStatus.APPROVED);

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        when(passPriceService.getPrice(PassType.STUDENT, PassValidity.ONE_MONTH)).thenReturn(AMOUNT);

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);

            if (payment.getPaymentId() == null) {
                payment.setPaymentId(PAYMENT_ID);
            }

            return payment;
        });

        when(razorpayService.createOrder(AMOUNT, APPLICATION_NUMBER)).thenReturn(RAZORPAY_ORDER_ID);

        when(razorpayService.getKeyId()).thenReturn(RAZORPAY_KEY_ID);

        // Act
        PaymentInitiationResponseDto response = paymentService.initiatePayment(APPLICATION_ID, USER_ID);

        // Assert
        assertEquals(PAYMENT_ID, response.getPaymentId());
        assertEquals(APPLICATION_ID, response.getApplicationId());
        assertEquals(AMOUNT, response.getAmount());
        assertEquals(AMOUNT_IN_PAISE, response.getAmountInPaise());
        assertEquals("INR", response.getCurrency());
        assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());
        assertEquals(RAZORPAY_KEY_ID, response.getRazorpayKeyId());
        assertEquals(RAZORPAY_ORDER_ID, response.getRazorpayOrderId());

        verify(passRepository).existsByUserAndPassStatusAndValidUntilGreaterThanEqual(application.getPassenger(), PassStatus.ACTIVE, FIXED_DATE);

        verify(paymentRepository).existsByPassApplicationApplicationIdAndPaymentStatus(APPLICATION_ID, PaymentStatus.SUCCESS);

        verify(paymentRepository, times(2)).save(any(Payment.class));

        verify(razorpayService).createOrder(AMOUNT, APPLICATION_NUMBER);

        verifyNoInteractions(passService, modelMapper);
    }


    // -------------------------------------------------------------------------
    // completeSuccessfulPayment
    // -------------------------------------------------------------------------

    @Test
    void completeSuccessfulPayment_alreadySuccessful_returnsExistingPaymentWithoutSavingOrIssuingPass() {

        // Arrange
        Payment payment = paymentWithStatus(PaymentStatus.SUCCESS);

        // Act
        Payment result = paymentService.completeSuccessfulPayment(payment, RAZORPAY_PAYMENT_ID, PAYMENT_METHOD);

        // Assert
        assertSame(payment, result);

        verifyNoInteractions(paymentRepository, passService);
    }

    @Test
    void completeSuccessfulPayment_nonPendingPayment_throwsBusinessException() {

        // Arrange
        Payment payment = paymentWithStatus(PaymentStatus.FAILED);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.completeSuccessfulPayment(payment, RAZORPAY_PAYMENT_ID, PAYMENT_METHOD));

        // Assert
        assertEquals("Only pending payments can be completed", exception.getMessage());

        verifyNoInteractions(paymentRepository, passService);
    }

    @Test
    void completeSuccessfulPayment_pendingPayment_marksSuccessAndIssuesPass() {

        // Arrange
        Payment payment = paymentWithStatus(PaymentStatus.PENDING);

        when(paymentRepository.save(payment)).thenReturn(payment);

        // Act
        Payment result = paymentService.completeSuccessfulPayment(payment, RAZORPAY_PAYMENT_ID, PAYMENT_METHOD);

        // Assert
        assertSame(payment, result);

        assertEquals(PaymentStatus.SUCCESS, payment.getPaymentStatus());

        assertEquals(RAZORPAY_PAYMENT_ID, payment.getGatewayPaymentId());

        assertEquals(PAYMENT_METHOD, payment.getPaymentMethod());

        assertNotNull(payment.getPaidAt());

        verify(paymentRepository).save(payment);

        verify(passService).issuePass(APPLICATION_ID);
    }


    // -------------------------------------------------------------------------
    // verifyAndCompletePayment
    // -------------------------------------------------------------------------

    @Test
    void verifyPayment_paymentNotFound_throwsResourceNotFound() throws RazorpayException {

        // Arrange
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> paymentService.verifyAndCompletePayment(PAYMENT_ID, RAZORPAY_ORDER_ID, RAZORPAY_PAYMENT_ID, RAZORPAY_SIGNATURE, USER_ID));

        // Assert
        assertEquals("Payment not found", exception.getMessage());

        verifyNoInteractions(passApplicationRepository, razorpayService, passService, modelMapper);
    }

    @Test
    void verifyPayment_applicationDoesNotBelongToUser_throwsResourceNotFound() throws RazorpayException {

        // Arrange
        Payment payment = paymentWithStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(null);

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> paymentService.verifyAndCompletePayment(PAYMENT_ID, RAZORPAY_ORDER_ID, RAZORPAY_PAYMENT_ID, RAZORPAY_SIGNATURE, USER_ID));

        // Assert
        assertEquals("Pass application not found", exception.getMessage());

        verifyNoInteractions(razorpayService, passService, modelMapper);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void verifyPayment_alreadySuccessful_returnsExistingPaymentWithoutGatewayVerification() throws RazorpayException {

        // Arrange
        Payment payment = paymentWithStatus(PaymentStatus.SUCCESS);

        PassApplication application = payment.getPassApplication();

        PaymentResponseDto expectedResponse = samplePaymentResponse(PaymentStatus.SUCCESS);

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        when(modelMapper.map(payment, PaymentResponseDto.class)).thenReturn(expectedResponse);

        // Act
        PaymentResponseDto result = paymentService.verifyAndCompletePayment(PAYMENT_ID, RAZORPAY_ORDER_ID, RAZORPAY_PAYMENT_ID, RAZORPAY_SIGNATURE, USER_ID);

        // Assert
        assertSame(expectedResponse, result);

        verifyNoInteractions(razorpayService, passService);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void verifyPayment_nonPendingPayment_throwsBusinessException() throws RazorpayException {

        // Arrange
        Payment payment = paymentWithStatus(PaymentStatus.FAILED);

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(payment.getPassApplication());

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.verifyAndCompletePayment(PAYMENT_ID, RAZORPAY_ORDER_ID, RAZORPAY_PAYMENT_ID, RAZORPAY_SIGNATURE, USER_ID));

        // Assert
        assertEquals("Only pending payments can be verified", exception.getMessage());

        verifyNoInteractions(razorpayService, passService, modelMapper);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void verifyPayment_orderIdMismatch_throwsBusinessException() throws RazorpayException {

        // Arrange
        Payment payment = paymentWithStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(payment.getPassApplication());

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.verifyAndCompletePayment(PAYMENT_ID, "wrong_order_id", RAZORPAY_PAYMENT_ID, RAZORPAY_SIGNATURE, USER_ID));

        // Assert
        assertEquals("Invalid Razorpay order", exception.getMessage());

        verifyNoInteractions(razorpayService, passService, modelMapper);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void verifyPayment_nullOrderId_throwsBusinessException() throws RazorpayException {

        // Arrange
        Payment payment = paymentWithStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(payment.getPassApplication());

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.verifyAndCompletePayment(PAYMENT_ID, null, RAZORPAY_PAYMENT_ID, RAZORPAY_SIGNATURE, USER_ID));

        // Assert
        assertEquals("Invalid Razorpay order", exception.getMessage());

        verifyNoInteractions(razorpayService, passService, modelMapper);
    }

    @Test
    void verifyPayment_invalidSignature_throwsBusinessException() throws RazorpayException {

        // Arrange
        Payment payment = paymentWithStatus(PaymentStatus.PENDING);

        stubOwnedPayment(payment);

        when(razorpayService.verifyPaymentSignature(RAZORPAY_ORDER_ID, RAZORPAY_PAYMENT_ID, RAZORPAY_SIGNATURE)).thenReturn(false);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.verifyAndCompletePayment(PAYMENT_ID, RAZORPAY_ORDER_ID, RAZORPAY_PAYMENT_ID, RAZORPAY_SIGNATURE, USER_ID));

        // Assert
        assertEquals("Invalid Razorpay payment signature", exception.getMessage());

        verify(razorpayService, never()).fetchPayment(any());

        verifyNoInteractions(passService, modelMapper);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void verifyPayment_gatewayPaymentIdMismatch_throwsBusinessException() throws RazorpayException {

        // Arrange
        Payment payment = paymentWithStatus(PaymentStatus.PENDING);

        stubOwnedPayment(payment);
        stubValidSignature();

        com.razorpay.Payment gatewayPayment = gatewayPayment("different_payment_id", RAZORPAY_ORDER_ID, AMOUNT_IN_PAISE, "INR", true, "captured", PAYMENT_METHOD);

        when(razorpayService.fetchPayment(RAZORPAY_PAYMENT_ID)).thenReturn(gatewayPayment);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.verifyAndCompletePayment(PAYMENT_ID, RAZORPAY_ORDER_ID, RAZORPAY_PAYMENT_ID, RAZORPAY_SIGNATURE, USER_ID));

        // Assert
        assertEquals("Invalid Razorpay payment", exception.getMessage());

        verifyNoInteractions(passService, modelMapper);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void verifyPayment_gatewayOrderIdMismatch_throwsBusinessException() throws RazorpayException {

        // Arrange
        Payment payment = paymentWithStatus(PaymentStatus.PENDING);

        stubOwnedPayment(payment);
        stubValidSignature();

        com.razorpay.Payment gatewayPayment = gatewayPayment(RAZORPAY_PAYMENT_ID, "different_order_id", AMOUNT_IN_PAISE, "INR", true, "captured", PAYMENT_METHOD);

        when(razorpayService.fetchPayment(RAZORPAY_PAYMENT_ID)).thenReturn(gatewayPayment);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.verifyAndCompletePayment(PAYMENT_ID, RAZORPAY_ORDER_ID, RAZORPAY_PAYMENT_ID, RAZORPAY_SIGNATURE, USER_ID));

        // Assert
        assertEquals("Razorpay payment does not belong to this order", exception.getMessage());

        verifyNoInteractions(passService, modelMapper);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void verifyPayment_invalidCurrency_throwsBusinessException() throws RazorpayException {

        // Arrange
        Payment payment = paymentWithStatus(PaymentStatus.PENDING);

        stubOwnedPayment(payment);
        stubValidSignature();

        com.razorpay.Payment gatewayPayment = gatewayPayment(RAZORPAY_PAYMENT_ID, RAZORPAY_ORDER_ID, AMOUNT_IN_PAISE, "USD", true, "captured", PAYMENT_METHOD);

        when(razorpayService.fetchPayment(RAZORPAY_PAYMENT_ID)).thenReturn(gatewayPayment);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.verifyAndCompletePayment(PAYMENT_ID, RAZORPAY_ORDER_ID, RAZORPAY_PAYMENT_ID, RAZORPAY_SIGNATURE, USER_ID));

        // Assert
        assertEquals("Invalid payment currency", exception.getMessage());

        verifyNoInteractions(passService, modelMapper);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void verifyPayment_amountMismatch_throwsBusinessException() throws RazorpayException {

        // Arrange
        Payment payment = paymentWithStatus(PaymentStatus.PENDING);

        stubOwnedPayment(payment);
        stubValidSignature();

        com.razorpay.Payment gatewayPayment = gatewayPayment(RAZORPAY_PAYMENT_ID, RAZORPAY_ORDER_ID, 99999L, "INR", true, "captured", PAYMENT_METHOD);

        when(razorpayService.fetchPayment(RAZORPAY_PAYMENT_ID)).thenReturn(gatewayPayment);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.verifyAndCompletePayment(PAYMENT_ID, RAZORPAY_ORDER_ID, RAZORPAY_PAYMENT_ID, RAZORPAY_SIGNATURE, USER_ID));

        // Assert
        assertEquals("Razorpay payment amount mismatch", exception.getMessage());

        verifyNoInteractions(passService, modelMapper);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void verifyPayment_notCapturedFlag_throwsBusinessException() throws RazorpayException {

        // Arrange
        Payment payment = paymentWithStatus(PaymentStatus.PENDING);

        stubOwnedPayment(payment);
        stubValidSignature();

        com.razorpay.Payment gatewayPayment = gatewayPayment(RAZORPAY_PAYMENT_ID, RAZORPAY_ORDER_ID, AMOUNT_IN_PAISE, "INR", false, "captured", PAYMENT_METHOD);

        when(razorpayService.fetchPayment(RAZORPAY_PAYMENT_ID)).thenReturn(gatewayPayment);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.verifyAndCompletePayment(PAYMENT_ID, RAZORPAY_ORDER_ID, RAZORPAY_PAYMENT_ID, RAZORPAY_SIGNATURE, USER_ID));

        // Assert
        assertEquals("Razorpay payment is not captured", exception.getMessage());

        verifyNoInteractions(passService, modelMapper);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void verifyPayment_statusNotCaptured_throwsBusinessException() throws RazorpayException {

        // Arrange
        Payment payment = paymentWithStatus(PaymentStatus.PENDING);

        stubOwnedPayment(payment);
        stubValidSignature();

        com.razorpay.Payment gatewayPayment = gatewayPayment(RAZORPAY_PAYMENT_ID, RAZORPAY_ORDER_ID, AMOUNT_IN_PAISE, "INR", true, "authorized", PAYMENT_METHOD);

        when(razorpayService.fetchPayment(RAZORPAY_PAYMENT_ID)).thenReturn(gatewayPayment);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.verifyAndCompletePayment(PAYMENT_ID, RAZORPAY_ORDER_ID, RAZORPAY_PAYMENT_ID, RAZORPAY_SIGNATURE, USER_ID));

        // Assert
        assertEquals("Razorpay payment is not captured", exception.getMessage());

        verifyNoInteractions(passService, modelMapper);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void verifyPayment_validGatewayPayment_completesPaymentAndIssuesPass() throws RazorpayException {

        // Arrange
        Payment payment = paymentWithStatus(PaymentStatus.PENDING);

        PaymentResponseDto expectedResponse = samplePaymentResponse(PaymentStatus.SUCCESS);

        stubOwnedPayment(payment);
        stubValidSignature();

        com.razorpay.Payment gatewayPayment = gatewayPayment(RAZORPAY_PAYMENT_ID, RAZORPAY_ORDER_ID, AMOUNT_IN_PAISE, "INR", true, "captured", PAYMENT_METHOD);

        when(razorpayService.fetchPayment(RAZORPAY_PAYMENT_ID)).thenReturn(gatewayPayment);

        when(paymentRepository.save(payment)).thenReturn(payment);

        when(modelMapper.map(payment, PaymentResponseDto.class)).thenReturn(expectedResponse);

        // Act
        PaymentResponseDto result = paymentService.verifyAndCompletePayment(PAYMENT_ID, RAZORPAY_ORDER_ID, RAZORPAY_PAYMENT_ID, RAZORPAY_SIGNATURE, USER_ID);

        // Assert
        assertSame(expectedResponse, result);

        assertEquals(PaymentStatus.SUCCESS, payment.getPaymentStatus());

        assertEquals(RAZORPAY_PAYMENT_ID, payment.getGatewayPaymentId());

        assertEquals(PAYMENT_METHOD, payment.getPaymentMethod());

        assertNotNull(payment.getPaidAt());

        verify(razorpayService).verifyPaymentSignature(RAZORPAY_ORDER_ID, RAZORPAY_PAYMENT_ID, RAZORPAY_SIGNATURE);

        verify(razorpayService).fetchPayment(RAZORPAY_PAYMENT_ID);

        verify(paymentRepository).save(payment);

        verify(passService).issuePass(APPLICATION_ID);

        verify(modelMapper).map(payment, PaymentResponseDto.class);
    }


    // -------------------------------------------------------------------------
    // markPaymentFailed
    // -------------------------------------------------------------------------

    @Test
    void markPaymentFailed_paymentNotFound_throwsResourceNotFound() {

        // Arrange
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> paymentService.markPaymentFailed(PAYMENT_ID));

        // Assert
        assertEquals("Payment not found", exception.getMessage());

        verifyNoInteractions(modelMapper);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void markPaymentFailed_nonPendingPayment_throwsBusinessException() {

        // Arrange
        Payment payment = paymentWithStatus(PaymentStatus.SUCCESS);

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.markPaymentFailed(PAYMENT_ID));

        // Assert
        assertEquals("Only pending payments can be marked as failed", exception.getMessage());

        verifyNoInteractions(modelMapper);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void markPaymentFailed_pendingPayment_marksFailed() {

        // Arrange
        Payment payment = paymentWithStatus(PaymentStatus.PENDING);

        PaymentResponseDto expectedResponse = samplePaymentResponse(PaymentStatus.FAILED);

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

        when(paymentRepository.save(payment)).thenReturn(payment);

        when(modelMapper.map(payment, PaymentResponseDto.class)).thenReturn(expectedResponse);

        // Act
        PaymentResponseDto result = paymentService.markPaymentFailed(PAYMENT_ID);

        // Assert
        assertSame(expectedResponse, result);

        assertEquals(PaymentStatus.FAILED, payment.getPaymentStatus());

        verify(paymentRepository).save(payment);

        verify(modelMapper).map(payment, PaymentResponseDto.class);
    }


    // -------------------------------------------------------------------------
    // getOwnPayment
    // -------------------------------------------------------------------------

    @Test
    void getOwnPayment_paymentNotFound_throwsResourceNotFound() {

        // Arrange
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> paymentService.getOwnPayment(PAYMENT_ID, USER_ID));

        // Assert
        assertEquals("Payment not found", exception.getMessage());

        verifyNoInteractions(passApplicationRepository, modelMapper);
    }

    @Test
    void getOwnPayment_applicationNotOwnedByUser_throwsResourceNotFound() {

        // Arrange
        Payment payment = paymentWithStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(null);

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> paymentService.getOwnPayment(PAYMENT_ID, USER_ID));

        // Assert
        assertEquals("Pass application not found", exception.getMessage());

        verifyNoInteractions(modelMapper);
    }

    @Test
    void getOwnPayment_ownedPayment_returnsMappedDto() {

        // Arrange
        Payment payment = paymentWithStatus(PaymentStatus.PENDING);

        PaymentResponseDto expectedResponse = samplePaymentResponse(PaymentStatus.PENDING);

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(payment.getPassApplication());

        when(modelMapper.map(payment, PaymentResponseDto.class)).thenReturn(expectedResponse);

        // Act
        PaymentResponseDto result = paymentService.getOwnPayment(PAYMENT_ID, USER_ID);

        // Assert
        assertSame(expectedResponse, result);

        verify(modelMapper).map(payment, PaymentResponseDto.class);
    }


    // -------------------------------------------------------------------------
    // getAllPaymentsByApplication
    // -------------------------------------------------------------------------

    @Test
    void getAllPayments_applicationNotOwnedByUser_throwsResourceNotFound() {

        // Arrange
        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(null);

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> paymentService.getAllPaymentsByApplication(APPLICATION_ID, USER_ID));

        // Assert
        assertEquals("Pass application not found", exception.getMessage());

        verify(paymentRepository, never()).findAllByPassApplicationApplicationIdOrderByInitiatedAtDesc(APPLICATION_ID);

        verifyNoInteractions(modelMapper);
    }

    @Test
    void getAllPayments_noPayments_returnsEmptyList() {

        // Arrange
        PassApplication application = applicationWithStatus(ApplicationStatus.APPROVED);

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        when(paymentRepository.findAllByPassApplicationApplicationIdOrderByInitiatedAtDesc(APPLICATION_ID)).thenReturn(List.of());

        // Act
        List<PaymentResponseDto> result = paymentService.getAllPaymentsByApplication(APPLICATION_ID, USER_ID);

        // Assert
        assertTrue(result.isEmpty());

        verifyNoInteractions(modelMapper);
    }

    @Test
    void getAllPayments_existingPayments_returnsMappedDtos() {

        // Arrange
        PassApplication application = applicationWithStatus(ApplicationStatus.APPROVED);

        Payment firstPayment = paymentWithStatus(PaymentStatus.FAILED);

        Payment secondPayment = Payment.builder().paymentId(21L).passApplication(application).amount(AMOUNT).paymentStatus(PaymentStatus.PENDING).gatewayOrderId("order_456").build();

        PaymentResponseDto firstDto = samplePaymentResponse(PaymentStatus.FAILED);

        PaymentResponseDto secondDto = PaymentResponseDto.builder().paymentId(21L).applicationId(APPLICATION_ID).amount(AMOUNT).paymentStatus(PaymentStatus.PENDING).gatewayOrderId("order_456").build();

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        when(paymentRepository.findAllByPassApplicationApplicationIdOrderByInitiatedAtDesc(APPLICATION_ID)).thenReturn(List.of(firstPayment, secondPayment));

        when(modelMapper.map(firstPayment, PaymentResponseDto.class)).thenReturn(firstDto);

        when(modelMapper.map(secondPayment, PaymentResponseDto.class)).thenReturn(secondDto);

        // Act
        List<PaymentResponseDto> result = paymentService.getAllPaymentsByApplication(APPLICATION_ID, USER_ID);

        // Assert
        assertEquals(2, result.size());

        assertSame(firstDto, result.get(0));

        assertSame(secondDto, result.get(1));

        verify(modelMapper).map(firstPayment, PaymentResponseDto.class);

        verify(modelMapper).map(secondPayment, PaymentResponseDto.class);
    }


    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Users passenger() {
        return Users.builder().userId(USER_ID).username("passenger").role(Role.PASSENGER).build();
    }

    private PassApplication applicationWithStatus(ApplicationStatus status) {

        return PassApplication.builder().applicationId(APPLICATION_ID).applicationNumber(APPLICATION_NUMBER).passenger(passenger()).passType(PassType.STUDENT).passValidity(PassValidity.ONE_MONTH).applicationStatus(status).build();
    }

    private Payment paymentWithStatus(PaymentStatus status) {

        return Payment.builder().paymentId(PAYMENT_ID).passApplication(applicationWithStatus(ApplicationStatus.APPROVED)).amount(AMOUNT).paymentStatus(status).gatewayOrderId(RAZORPAY_ORDER_ID).build();
    }

    private PaymentResponseDto samplePaymentResponse(PaymentStatus status) {

        return PaymentResponseDto.builder().paymentId(PAYMENT_ID).applicationId(APPLICATION_ID).amount(AMOUNT).paymentStatus(status).gatewayOrderId(RAZORPAY_ORDER_ID).build();
    }

    private void stubOwnedPayment(Payment payment) {

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(payment.getPassApplication());
    }

    private void stubValidSignature() throws RazorpayException {

        when(razorpayService.verifyPaymentSignature(RAZORPAY_ORDER_ID, RAZORPAY_PAYMENT_ID, RAZORPAY_SIGNATURE)).thenReturn(true);
    }

    private com.razorpay.Payment gatewayPayment(String paymentId, String orderId, long amountInPaise, String currency, boolean captured, String status, String method) {

        com.razorpay.Payment gatewayPayment = mock(com.razorpay.Payment.class);

        doReturn(paymentId).when(gatewayPayment).get("id");

        doReturn(orderId).when(gatewayPayment).get("order_id");

        doReturn(amountInPaise).when(gatewayPayment).get("amount");

        doReturn(status).when(gatewayPayment).get("status");

        doReturn(captured).when(gatewayPayment).get("captured");

        doReturn(method).when(gatewayPayment).get("method");

        doReturn(currency).when(gatewayPayment).get("currency");

        return gatewayPayment;
    }
}