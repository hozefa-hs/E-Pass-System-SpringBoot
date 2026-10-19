package com.porfolio.EPassSystemSpringboot.dtos;

import com.porfolio.EPassSystemSpringboot.enums.PassStatus;
import com.porfolio.EPassSystemSpringboot.enums.PassType;
import com.porfolio.EPassSystemSpringboot.enums.PassValidity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassValidationResponseDto {

    private boolean valid;
    private String message;

    private String passNumber;
    private String passengerUsername;

    private PassType passType;
    private PassValidity passValidity;
    private PassStatus passStatus;

    private LocalDate validFrom;
    private LocalDate validUntil;

}