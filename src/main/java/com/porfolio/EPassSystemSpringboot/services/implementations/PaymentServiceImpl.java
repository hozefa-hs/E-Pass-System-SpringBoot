package com.porfolio.EPassSystemSpringboot.services.implementations;

import com.porfolio.EPassSystemSpringboot.dtos.PaymentInitiationResponseDto;
import com.porfolio.EPassSystemSpringboot.dtos.PaymentResponseDto;
import com.porfolio.EPassSystemSpringboot.entities.PassApplication;
import com.porfolio.EPassSystemSpringboot.entities.Payment;
import com.porfolio.EPassSystemSpringboot.enums.ApplicationStatus;
import com.porfolio.EPassSystemSpringboot.enums.PassStatus;
import com.porfolio.EPassSystemSpringboot.enums.PaymentStatus;
import com.porfolio.EPassSystemSpringboot.exceptions.BusinessException;
import com.porfolio.EPassSystemSpringboot.exceptions.ResourceNotFoundException;
import com.porfolio.EPassSystemSpringboot.repositories.PassApplicationRepository;
import com.porfolio.EPassSystemSpringboot.repositories.PassRepository;
import com.porfolio.EPassSystemSpringboot.repositories.PaymentRepository;
import com.porfolio.EPassSystemSpringboot.services.PassPriceService;
import com.porfolio.EPassSystemSpringboot.services.PassService;
import com.porfolio.EPassSystemSpringboot.services.PaymentService;
import com.porfolio.EPassSystemSpringboot.services.RazorpayService;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final ModelMapper modelMapper;
    private final PassApplicationRepository passApplicationRepository;
    private final PassRepository passRepository;
    private final PaymentRepository paymentRepository;
    private final PassPriceService passPriceService;
    private final PassService passService;
    private final RazorpayService razorpayService;
    private final Clock businessClock;


    @Override
    @Transactional
    public PaymentInitiationResponseDto initiatePayment(Long applicationId, Long userId) throws RazorpayException {

        PassApplication passApplication = passApplicationRepository.findByApplicationIdAndPassengerUserId(applicationId, userId);

        if (passApplication == null) {
            throw new ResourceNotFoundException("Pass application not found");
        }

        if (passApplication.getApplicationStatus() != ApplicationStatus.APPROVED) {
            throw new BusinessException("Payment can only be initiated for an approved application");
        }

        if (passRepository.existsByUserAndPassStatusAndValidUntilGreaterThanEqual(passApplication.getPassenger(), PassStatus.ACTIVE, LocalDate.now(businessClock))) {
            throw new BusinessException("Passenger already has an active pass");
        }

        if (paymentRepository.existsByPassApplicationApplicationIdAndPaymentStatus(applicationId, PaymentStatus.SUCCESS)) {
            throw new BusinessException("Payment has already been completed for this application");
        }


        BigDecimal amount = passPriceService.getPrice(passApplication.getPassType(), passApplication.getPassValidity());

        Payment payment = Payment.builder().passApplication(passApplication).amount(amount).paymentStatus(PaymentStatus.PENDING).initiatedAt(LocalDateTime.now()).build();

        Payment savedPayment = paymentRepository.save(payment);

        String razorpayOrderId = razorpayService.createOrder(amount, passApplication.getApplicationNumber());

        savedPayment.setGatewayOrderId(razorpayOrderId);

        savedPayment = paymentRepository.save(savedPayment);

        long amountInPaise = savedPayment.getAmount().multiply(BigDecimal.valueOf(100)).longValueExact();

        return PaymentInitiationResponseDto.builder().paymentId(savedPayment.getPaymentId()).applicationId(passApplication.getApplicationId()).amount(savedPayment.getAmount()).amountInPaise(amountInPaise).currency("INR").paymentStatus(savedPayment.getPaymentStatus()).razorpayKeyId(razorpayService.getKeyId()).razorpayOrderId(savedPayment.getGatewayOrderId()).build();
    }


    @Override
    public Payment completeSuccessfulPayment(Payment payment, String razorpayPaymentId, String paymentMethod) {

        // Idempotency
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return payment;
        }

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("Only pending payments can be completed");
        }

        payment.setGatewayPaymentId(razorpayPaymentId);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        // Issue ACTIVE pass after successful payment
        passService.issuePass(payment.getPassApplication().getApplicationId());

        return savedPayment;
    }


    @Override
    @Transactional
    public PaymentResponseDto verifyAndCompletePayment(Long paymentId, String razorpayOrderId, String razorpayPaymentId, String razorpaySignature, Long userId) throws RazorpayException {

        //1. Payment exists
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        PassApplication passApplication = passApplicationRepository.findByApplicationIdAndPassengerUserId(payment.getPassApplication().getApplicationId(), userId);

        if (passApplication == null) {
            throw new ResourceNotFoundException("Pass application not found");
        }

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return modelMapper.map(payment, PaymentResponseDto.class);
        }

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("Only pending payments can be verified");
        }

        // Verify that the received order belongs to our payment
        if (!payment.getGatewayOrderId().equals(razorpayOrderId)) {
            throw new BusinessException("Invalid Razorpay order");
        }

        boolean isSignatureValid = razorpayService.verifyPaymentSignature(payment.getGatewayOrderId(), razorpayPaymentId, razorpaySignature);

        if (!isSignatureValid) {
            throw new BusinessException("Invalid Razorpay payment signature");
        }


        com.razorpay.Payment razorpayPayment = razorpayService.fetchPayment(razorpayPaymentId);


        Object gatewayPaymentIdValue = razorpayPayment.get("id");
        Object gatewayOrderIdValue = razorpayPayment.get("order_id");
        Object gatewayAmountValue = razorpayPayment.get("amount");
        Object statusValue = razorpayPayment.get("status");
        Object capturedValue = razorpayPayment.get("captured");
        Object paymentMethodValue = razorpayPayment.get("method");
        Object currencyValue = razorpayPayment.get("currency");

        String gatewayPaymentId = String.valueOf(gatewayPaymentIdValue);
        String gatewayOrderId = String.valueOf(gatewayOrderIdValue);
        long gatewayAmount = Long.parseLong(String.valueOf(gatewayAmountValue));
        String status = String.valueOf(statusValue);
        String currency = String.valueOf(currencyValue);
        boolean captured = Boolean.parseBoolean(String.valueOf(capturedValue));
        long expectedAmountInPaise = payment.getAmount().multiply(BigDecimal.valueOf(100)).longValueExact();


        // Verify payment ID
        if (!gatewayPaymentId.equals(razorpayPaymentId)) {
            throw new BusinessException("Invalid Razorpay payment");
        }

        // Verify order ID
        if (!payment.getGatewayOrderId().equals(gatewayOrderId)) {
            throw new BusinessException("Razorpay payment does not belong to this order");
        }

        //Verify currency
        if (!"INR".equals(currency)) {
            throw new BusinessException("Invalid payment currency");
        }

        // Verify amount
        if (gatewayAmount != expectedAmountInPaise) {
            throw new BusinessException("Razorpay payment amount mismatch");
        }

        //Payment must actually be captured
        if (!captured || !"captured".equalsIgnoreCase(status)) {
            throw new BusinessException("Razorpay payment is not captured");
        }


        //Complete local payment + issue pass
        String paymentMethod = String.valueOf(paymentMethodValue);

        Payment completedSuccessfulPayment = completeSuccessfulPayment(payment, razorpayPaymentId, paymentMethod);

        return modelMapper.map(completedSuccessfulPayment, PaymentResponseDto.class);

    }


    @Override
    @Transactional
    public PaymentResponseDto markPaymentFailed(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("Only pending payments can be marked as failed");
        }

        payment.setPaymentStatus(PaymentStatus.FAILED);

        Payment savedPayment = paymentRepository.save(payment);

        return modelMapper.map(savedPayment, PaymentResponseDto.class);
    }

    @Override
    public PaymentResponseDto getOwnPayment(Long paymentId, Long userId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        PassApplication passApplication = passApplicationRepository.findByApplicationIdAndPassengerUserId(payment.getPassApplication().getApplicationId(), userId);

        if (passApplication == null) {
            throw new ResourceNotFoundException("Pass application not found");
        }

        return modelMapper.map(payment, PaymentResponseDto.class);
    }

    @Override
    public List<PaymentResponseDto> getAllPaymentsByApplication(Long applicationId, Long userId) {

        PassApplication passApplication = passApplicationRepository.findByApplicationIdAndPassengerUserId(applicationId, userId);

        if (passApplication == null) {
            throw new ResourceNotFoundException("Pass application not found");
        }

        List<Payment> paymentList = paymentRepository.findAllByPassApplicationApplicationIdOrderByInitiatedAtDesc(applicationId);

        return paymentList.stream().map(payment -> modelMapper.map(payment, PaymentResponseDto.class)).toList();
    }

}
