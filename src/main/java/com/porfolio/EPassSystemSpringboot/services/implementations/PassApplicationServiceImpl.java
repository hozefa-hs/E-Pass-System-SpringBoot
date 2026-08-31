package com.porfolio.EPassSystemSpringboot.services.implementations;

import com.porfolio.EPassSystemSpringboot.dtos.CreatePassApplicationDto;
import com.porfolio.EPassSystemSpringboot.dtos.PassApplicationResponseDto;
import com.porfolio.EPassSystemSpringboot.entities.PassApplication;
import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.enums.ApplicationStatus;
import com.porfolio.EPassSystemSpringboot.enums.PassStatus;
import com.porfolio.EPassSystemSpringboot.enums.Role;
import com.porfolio.EPassSystemSpringboot.exceptions.ResourceNotFoundException;
import com.porfolio.EPassSystemSpringboot.repositories.PassApplicationRepository;
import com.porfolio.EPassSystemSpringboot.repositories.PassRepository;
import com.porfolio.EPassSystemSpringboot.repositories.UserRepository;
import com.porfolio.EPassSystemSpringboot.services.PassApplicationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PassApplicationServiceImpl implements PassApplicationService {

    private final UserRepository userRepository;
    private final PassRepository passRepository;
    private final PassApplicationRepository passApplicationRepository;
    private final ModelMapper modelMapper;


    @Override
    public PassApplicationResponseDto createApplication(CreatePassApplicationDto createPassApplicationDto, Long userId) {

        Users passenger = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found while creating application"));

        if (passenger.getRole() != Role.PASSENGER) {
            throw new ResourceNotFoundException("Only passengers can apply for pass");
        }

        if (passRepository.existsByUserAndPassStatus(passenger, PassStatus.ACTIVE)) {
            throw new ResourceNotFoundException("You already have an active pass");
        }

        if (passApplicationRepository.existsByPassengerAndApplicationStatus(passenger, ApplicationStatus.PENDING)) {
            throw new ResourceNotFoundException("You already have a pending application");
        }

        PassApplication passApplication = modelMapper.map(createPassApplicationDto, PassApplication.class);

        passApplication.setPassenger(passenger);
        passApplication.setApplicationStatus(ApplicationStatus.PENDING);
        passApplication.setAppliedAt(LocalDateTime.now());
        passApplication.setApplicationNumber(generateApplicationNumber());

        PassApplication savedApplication = passApplicationRepository.save(passApplication);

        return modelMapper.map(savedApplication, PassApplicationResponseDto.class);
    }

    @Override
    public List<PassApplicationResponseDto> getOwnApplications(Long userId) {
        List<PassApplication> applicationList = passApplicationRepository.findAllByPassengerUserId(userId);

        return applicationList
                .stream()
                .map(passApplication -> modelMapper.map(passApplication, PassApplicationResponseDto.class))
                .toList();
    }

    @Override
    public List<PassApplicationResponseDto> getPendingApplications() {

        List<PassApplication> allPendingApplications = passApplicationRepository.findAllByApplicationStatus(ApplicationStatus.PENDING);

        return allPendingApplications
                .stream()
                .map(passApplication -> modelMapper.map(passApplication, PassApplicationResponseDto.class))
                .toList();
    }

    @Override
    public PassApplicationResponseDto getApplicationById(Long applicationId) {
        PassApplication passApplication = passApplicationRepository.findById(applicationId).orElseThrow(() -> new ResourceNotFoundException("Pass application with id " + applicationId + " not found"));
        return modelMapper.map(passApplication, PassApplicationResponseDto.class);
    }


    private String generateApplicationNumber() {
        return "APP-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}
