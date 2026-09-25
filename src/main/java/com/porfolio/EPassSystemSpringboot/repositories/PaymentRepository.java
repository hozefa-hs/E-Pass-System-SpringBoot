package com.porfolio.EPassSystemSpringboot.repositories;

import com.porfolio.EPassSystemSpringboot.entities.Payment;
import com.porfolio.EPassSystemSpringboot.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByPassApplicationApplicationIdOrderByInitiatedAtDesc(Long applicationId);

    Optional<Payment> findByGatewayOrderId(String gatewayOrderId);

    Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId);

    boolean existsByPassApplicationApplicationIdAndPaymentStatus(Long applicationId, PaymentStatus paymentStatus);

}
