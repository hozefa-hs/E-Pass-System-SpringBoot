package com.porfolio.EPassSystemSpringboot.dtos;

import com.porfolio.EPassSystemSpringboot.enums.PassStatus;
import com.porfolio.EPassSystemSpringboot.enums.PassType;
import com.porfolio.EPassSystemSpringboot.enums.PassValidity;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PassResponseDto {

    private Long passId;
    private String passNumber;
    private String qrToken;
    private Long applicationId;
    private Long passengerId;
    private PassType passType;
    private PassValidity passValidity;
    private PassStatus passStatus;
    private LocalDate validFrom;
    private LocalDate validUntil;

}
