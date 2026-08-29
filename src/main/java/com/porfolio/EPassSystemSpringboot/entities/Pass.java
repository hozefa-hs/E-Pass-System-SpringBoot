package com.porfolio.EPassSystemSpringboot.entities;

import com.porfolio.EPassSystemSpringboot.enums.PassStatus;
import com.porfolio.EPassSystemSpringboot.enums.PassType;
import com.porfolio.EPassSystemSpringboot.enums.PassValidity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "passes",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_pass_number", columnNames = "passNumber"),
                @UniqueConstraint(name = "unique_qr_token", columnNames = "qrToken")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long passId;

    @Column(nullable = false, unique = true)
    private String passNumber;

    @Column(nullable = false, unique = true)
    private String qrToken;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PassType passType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PassValidity passValidity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PassStatus passStatus;

    @Column(nullable = false)
    private LocalDate validFrom;

    @Column(nullable = false)
    private LocalDate validUntil;
}













