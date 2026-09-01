package com.porfolio.EPassSystemSpringboot.dtos;

import com.porfolio.EPassSystemSpringboot.enums.ApplicationStatus;
import com.porfolio.EPassSystemSpringboot.enums.PassType;
import com.porfolio.EPassSystemSpringboot.enums.PassValidity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PassApplicationResponseDto {

    private Long applicationId;
    private String applicationNumber;
    private Long passengerId;
    private PassType passType;
    private PassValidity passValidity;
    private ApplicationStatus applicationStatus;
    private LocalDateTime appliedAt;
    private String rejectionReason;
    private LocalDateTime reviewedAt;


}
