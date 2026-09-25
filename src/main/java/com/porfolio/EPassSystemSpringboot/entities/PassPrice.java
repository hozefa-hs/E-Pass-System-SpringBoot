package com.porfolio.EPassSystemSpringboot.entities;

import com.porfolio.EPassSystemSpringboot.enums.PassType;
import com.porfolio.EPassSystemSpringboot.enums.PassValidity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "pass_prices",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pass_type_validity",
                        columnNames = {"pass_type", "pass_validity"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long priceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pass_type", nullable = false)
    private PassType passType;

    @Enumerated(EnumType.STRING)
    @Column(name = "pass_validity", nullable = false)
    private PassValidity passValidity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
}

