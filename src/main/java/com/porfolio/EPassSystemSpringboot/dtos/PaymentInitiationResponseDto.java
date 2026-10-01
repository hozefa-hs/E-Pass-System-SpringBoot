package com.porfolio.EPassSystemSpringboot.dtos;

import com.porfolio.EPassSystemSpringboot.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInitiationResponseDto {

    private Long paymentId;

    private Long applicationId;

    private BigDecimal amount;

    private Long amountInPaise;

    private PaymentStatus paymentStatus;

    private String razorpayKeyId;

    private String razorpayOrderId;
}