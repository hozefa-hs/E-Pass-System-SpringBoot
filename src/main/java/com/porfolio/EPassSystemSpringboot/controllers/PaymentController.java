package com.porfolio.EPassSystemSpringboot.controllers;

import com.porfolio.EPassSystemSpringboot.dtos.PaymentInitiationResponseDto;
import com.porfolio.EPassSystemSpringboot.dtos.PaymentResponseDto;
import com.porfolio.EPassSystemSpringboot.dtos.RazorpayPaymentVerificationRequestDto;
import com.porfolio.EPassSystemSpringboot.services.PaymentService;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate/{applicationId}")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<PaymentInitiationResponseDto> initiatePayment(@PathVariable Long applicationId) throws RazorpayException {

        PaymentInitiationResponseDto paymentInitiationResponseDto = paymentService.initiatePayment(applicationId);

        return ResponseEntity.status(HttpStatus.CREATED).body(paymentInitiationResponseDto);
    }


    @PostMapping("/{paymentId}/verify")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<PaymentResponseDto> verifyPayment(
            @PathVariable Long paymentId,
            @RequestBody RazorpayPaymentVerificationRequestDto paymentVerificationRequestDto) throws RazorpayException {

        PaymentResponseDto paymentResponseDto = paymentService.verifyAndCompletePayment(
                paymentId,
                paymentVerificationRequestDto.getRazorpayOrderId(),
                paymentVerificationRequestDto.getRazorpayPaymentId(),
                paymentVerificationRequestDto.getRazorpaySignature()
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
    public ResponseEntity<PaymentResponseDto> getOwnPayment(@PathVariable Long paymentId) {
        PaymentResponseDto payment = paymentService.getOwnPayment(paymentId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/application/{applicationId}")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<List<PaymentResponseDto>> getAllPaymentsByApplication(@PathVariable Long applicationId) {

        List<PaymentResponseDto> paymentsByApplication = paymentService.getAllPaymentsByApplication(applicationId);
        return ResponseEntity.ok(paymentsByApplication);
    }


}
