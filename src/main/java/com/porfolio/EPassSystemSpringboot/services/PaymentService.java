package com.porfolio.EPassSystemSpringboot.services;

import com.porfolio.EPassSystemSpringboot.dtos.PaymentResponseDto;

import java.util.List;

public interface PaymentService {

    PaymentResponseDto initiatePayment(Long applicationId);

    PaymentResponseDto markPaymentSuccess(Long paymentId, String gatewayPaymentId);

    PaymentResponseDto markPaymentFailed(Long paymentId);

    PaymentResponseDto getOwnPayment(Long paymentId);

    List<PaymentResponseDto> getAllPaymentsByApplication(Long applicationId);

}
