package com.porfolio.EPassSystemSpringboot.services;

import com.porfolio.EPassSystemSpringboot.dtos.PaymentResponseDto;

public interface PaymentService {

    PaymentResponseDto initiatePayment(Long applicationId);

    PaymentResponseDto markPaymentSuccess(Long paymentId, String gatewayPaymentId);

    PaymentResponseDto markPaymentFailed(Long paymentId);

}
