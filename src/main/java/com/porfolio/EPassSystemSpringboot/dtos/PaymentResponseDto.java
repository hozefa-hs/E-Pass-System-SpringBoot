package com.porfolio.EPassSystemSpringboot.dtos;

import com.porfolio.EPassSystemSpringboot.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {

    private Long paymentId;
    private Long applicationId;
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
    private String gatewayOrderId;
    private String gatewayPaymentId;
    private LocalDateTime initiatedAt;
    private LocalDateTime paidAt;
}