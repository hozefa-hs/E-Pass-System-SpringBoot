package com.porfolio.EPassSystemSpringboot.controllers;

import com.porfolio.EPassSystemSpringboot.dtos.PaymentInitiationResponseDto;
import com.porfolio.EPassSystemSpringboot.dtos.PaymentResponseDto;
import com.porfolio.EPassSystemSpringboot.dtos.RazorpayPaymentVerificationRequestDto;
import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.services.PaymentService;
import com.razorpay.RazorpayException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate/{applicationId}")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<PaymentInitiationResponseDto> initiatePayment(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal Users user) throws RazorpayException {

        PaymentInitiationResponseDto paymentInitiationResponseDto = paymentService.initiatePayment(applicationId, user.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(paymentInitiationResponseDto);
    }


    @PostMapping("/{paymentId}/verify")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<PaymentResponseDto> verifyPayment(
            @PathVariable Long paymentId,
            @RequestBody @Valid RazorpayPaymentVerificationRequestDto paymentVerificationRequestDto,
            @AuthenticationPrincipal Users user) throws RazorpayException {

        PaymentResponseDto paymentResponseDto = paymentService.verifyAndCompletePayment(
                paymentId,
                paymentVerificationRequestDto.getRazorpayOrderId(),
                paymentVerificationRequestDto.getRazorpayPaymentId(),
                paymentVerificationRequestDto.getRazorpaySignature(),
                user.getUserId()
        );

        return ResponseEntity.ok(paymentResponseDto);
    }

/*
    POST /payments/initiate/{applicationId}
            ↓
    Create local PENDING Payment
            ↓
    Create Razorpay Order
            ↓
    Store gatewayOrderId
            ↓
    Payment happens at Razorpay
            ↓
    POST /payments/{paymentId}/verify
            ↓
    Compare order ID
            ↓
    Verify signature
            ↓
    Payment = SUCCESS
            ↓
    issuePass()
            ↓
    ACTIVE Pass
*/


    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<PaymentResponseDto> getOwnPayment(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal Users user) {
        PaymentResponseDto payment = paymentService.getOwnPayment(paymentId, user.getUserId());
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/application/{applicationId}")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<List<PaymentResponseDto>> getAllPaymentsByApplication(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal Users user) {

        List<PaymentResponseDto> paymentsByApplication = paymentService.getAllPaymentsByApplication(applicationId, user.getUserId());
        return ResponseEntity.ok(paymentsByApplication);
    }


}
