package com.porfolio.EPassSystemSpringboot.services;

import com.porfolio.EPassSystemSpringboot.dtos.PaymentInitiationResponseDto;
import com.porfolio.EPassSystemSpringboot.dtos.PaymentResponseDto;
import com.porfolio.EPassSystemSpringboot.entities.Payment;
import com.razorpay.RazorpayException;

import java.util.List;

public interface PaymentService {

    PaymentInitiationResponseDto initiatePayment(Long applicationId, Long userId) throws RazorpayException;

    PaymentResponseDto verifyAndCompletePayment(Long paymentId, String razorpayOrderId, String razorpayPaymentId, String razorpaySignature, Long userId) throws RazorpayException;

    Payment completeSuccessfulPayment(Payment payment, String razorpayPaymentId, String paymentMethod);

    PaymentResponseDto markPaymentFailed(Long paymentId);

    PaymentResponseDto getOwnPayment(Long paymentId, Long userId);

    List<PaymentResponseDto> getAllPaymentsByApplication(Long applicationId, Long userId);

}
