package com.porfolio.EPassSystemSpringboot.entities;

import com.porfolio.EPassSystemSpringboot.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private PassApplication passApplication;


    //decimal number with up to 10 total digits, of which 2 digits are after the decimal point.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(unique = true)
    private String gatewayOrderId;

    @Column(unique = true)
    private String gatewayPaymentId;

    private String paymentMethod;

    @Column(nullable = false)
    private LocalDateTime initiatedAt;

    private LocalDateTime paidAt;


}










