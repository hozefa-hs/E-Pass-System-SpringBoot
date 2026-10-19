package com.porfolio.EPassSystemSpringboot.services.implementations;

import com.porfolio.EPassSystemSpringboot.dtos.PassResponseDto;
import com.porfolio.EPassSystemSpringboot.dtos.PassValidationResponseDto;
import com.porfolio.EPassSystemSpringboot.entities.Pass;
import com.porfolio.EPassSystemSpringboot.entities.PassApplication;
import com.porfolio.EPassSystemSpringboot.enums.ApplicationStatus;
import com.porfolio.EPassSystemSpringboot.enums.PassStatus;
import com.porfolio.EPassSystemSpringboot.exceptions.BusinessException;
import com.porfolio.EPassSystemSpringboot.exceptions.ResourceNotFoundException;
import com.porfolio.EPassSystemSpringboot.repositories.PassApplicationRepository;
import com.porfolio.EPassSystemSpringboot.repositories.PassRepository;
import com.porfolio.EPassSystemSpringboot.services.PassService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PassServiceImpl implements PassService {

    private final PassApplicationRepository passApplicationRepository;
    private final PassRepository passRepository;
    private final ModelMapper modelMapper;
    private final Clock businessClock;


    @Override
    @Transactional
    public PassResponseDto issuePass(Long applicationId) {

        PassApplication passApplication = passApplicationRepository.findById(applicationId).orElseThrow(() -> new ResourceNotFoundException("Pass application not found"));

        if (passApplication.getApplicationStatus() != ApplicationStatus.APPROVED) {
            throw new BusinessException("Pass can only be issued for an approved application");
        }

        if (passRepository.existsByPassApplication(passApplication)) {
            throw new BusinessException("Pass already exists for application id : " + applicationId);
        }

        LocalDate today = LocalDate.now(businessClock);

        if (passRepository.existsByUserAndPassStatusAndValidUntilGreaterThanEqual(passApplication.getPassenger(), PassStatus.ACTIVE, today)) {
            throw new BusinessException("Passenger already has an active pass");
        }


        LocalDate validFrom = LocalDate.now(businessClock);
        LocalDate validUntil = validFrom.plusMonths(passApplication.getPassValidity().getMonths()).minusDays(1);

        Pass pass = Pass.builder().passNumber(generatePassNumber()).qrToken(generateQrToken()).passApplication(passApplication).user(passApplication.getPassenger()).passType(passApplication.getPassType()).passValidity(passApplication.getPassValidity()).passStatus(PassStatus.ACTIVE).validFrom(validFrom).validUntil(validUntil).build();

        Pass savedPass = passRepository.save(pass);

        return modelMapper.map(savedPass, PassResponseDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PassValidationResponseDto validatePass(String qrToken) {

        Pass pass = passRepository.findByQrToken(qrToken).orElseThrow(() -> new ResourceNotFoundException("Pass not found"));

        LocalDate today = LocalDate.now(businessClock);

        boolean valid;
        String message;

        if (today.isBefore(pass.getValidFrom())) {
            valid = false;
            message = "Pass is not yet valid.";
        } else if (today.isAfter(pass.getValidUntil())) {
            valid = false;
            message = "Pass has expired.";
        } else if (pass.getPassStatus() != PassStatus.ACTIVE) {
            valid = false;
            message = "Pass is not active.";
        } else {
            valid = true;
            message = "Pass is valid.";
        }

        return PassValidationResponseDto.builder()
                .valid(valid)
                .message(message)
                .passNumber(pass.getPassNumber())
                .passengerUsername(pass.getUser().getUsername())
                .passType(pass.getPassType())
                .passValidity(pass.getPassValidity())
                .passStatus(pass.getPassStatus())
                .validFrom(pass.getValidFrom())
                .validUntil(pass.getValidUntil())
                .build();
    }


    private String generatePassNumber() {
        return "PASS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateQrToken() {
        return UUID.randomUUID().toString();
    }

}
