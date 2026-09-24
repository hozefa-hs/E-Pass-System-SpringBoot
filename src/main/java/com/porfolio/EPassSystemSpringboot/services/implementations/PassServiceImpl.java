package com.porfolio.EPassSystemSpringboot.services.implementations;

import com.porfolio.EPassSystemSpringboot.dtos.PassResponseDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PassServiceImpl implements PassService {

    private final PassApplicationRepository passApplicationRepository;
    private final PassRepository passRepository;
    private final ModelMapper modelMapper;


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

        if (passRepository.existsByUserAndPassStatus(passApplication.getPassenger(), PassStatus.ACTIVE)) {
            throw new BusinessException("Passenger already has an active pass");
        }


        LocalDate validFrom = LocalDate.now();
        LocalDate validUntil = validFrom.plusMonths(passApplication.getPassValidity().getMonths());

        Pass pass = Pass.builder()
                .passNumber(generatePassNumber())
                .qrToken(generateQrToken())
                .passApplication(passApplication)
                .user(passApplication.getPassenger())
                .passType(passApplication.getPassType())
                .passValidity(passApplication.getPassValidity())
                .passStatus(PassStatus.ACTIVE)
                .validFrom(validFrom)
                .validUntil(validUntil)
                .build();

        Pass savedPass = passRepository.save(pass);

        return modelMapper.map(savedPass, PassResponseDto.class);
    }



    private String generatePassNumber() {
        return "PASS-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    private String generateQrToken() {
        return UUID.randomUUID().toString();
    }

}
