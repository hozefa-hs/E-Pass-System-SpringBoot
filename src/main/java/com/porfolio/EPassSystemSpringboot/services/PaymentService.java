package com.porfolio.EPassSystemSpringboot.services;

import com.porfolio.EPassSystemSpringboot.dtos.PaymentResponseDto;
import com.razorpay.RazorpayException;

import java.util.List;

public interface PaymentService {

    PaymentResponseDto initiatePayment(Long applicationId) throws RazorpayException;

    PaymentResponseDto verifyAndCompletePayment(Long paymentId, String razorpayOrderId, String razorpayPaymentId, String razorpaySignature);

    PaymentResponseDto markPaymentSuccess(Long paymentId, String gatewayPaymentId);

    PaymentResponseDto markPaymentFailed(Long paymentId);

    PaymentResponseDto getOwnPayment(Long paymentId);

    List<PaymentResponseDto> getAllPaymentsByApplication(Long applicationId);

}
