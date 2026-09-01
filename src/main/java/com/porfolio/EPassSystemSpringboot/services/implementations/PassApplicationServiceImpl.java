package com.porfolio.EPassSystemSpringboot.services.implementations;

import com.porfolio.EPassSystemSpringboot.dtos.CreatePassApplicationDto;
import com.porfolio.EPassSystemSpringboot.dtos.PassApplicationResponseDto;
import com.porfolio.EPassSystemSpringboot.entities.PassApplication;
import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.enums.ApplicationStatus;
import com.porfolio.EPassSystemSpringboot.enums.PassStatus;
import com.porfolio.EPassSystemSpringboot.enums.Role;
import com.porfolio.EPassSystemSpringboot.exceptions.BusinessException;
import com.porfolio.EPassSystemSpringboot.exceptions.ResourceNotFoundException;
import com.porfolio.EPassSystemSpringboot.repositories.PassApplicationRepository;
import com.porfolio.EPassSystemSpringboot.repositories.PassRepository;
import com.porfolio.EPassSystemSpringboot.repositories.UserRepository;
import com.porfolio.EPassSystemSpringboot.services.PassApplicationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
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
            throw new BusinessException("Only passengers can apply for pass");
        }

        if (passRepository.existsByUserAndPassStatus(passenger, PassStatus.ACTIVE)) {
            throw new BusinessException("You already have an active pass");
        }

        if (passApplicationRepository.existsByPassengerAndApplicationStatus(passenger, ApplicationStatus.PENDING)) {
            throw new BusinessException("You already have a pending application");
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
    public Page<PassApplicationResponseDto> getPendingApplications(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<PassApplication> allPendingApplications = passApplicationRepository.findAllByApplicationStatus(ApplicationStatus.PENDING, pageable);

        return allPendingApplications
                .map(passApplication -> modelMapper.map(passApplication, PassApplicationResponseDto.class));
    }

    @Override
    public PassApplicationResponseDto getApplicationById(Long applicationId, Long userId) {

        Users user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        //These conditions satisfy that when the user is passenger. they can only see their own application(ownership check)
        //and when the user is Pass Officer. He can see any application.
        PassApplication passApplication;
        if (user.getRole() == Role.PASSENGER) {
            passApplication = passApplicationRepository.findByApplicationIdAndPassengerUserId(applicationId, userId);

            // Application exists, but it does not belong to this passenger
            if (passApplication == null) {
                throw new AccessDeniedException("You are not authorized to view this application");
            }
        }
        else { // else condition when Role is Pass Officer
            passApplication = passApplicationRepository.findById(applicationId).orElseThrow(() -> new ResourceNotFoundException("Pass application with id " + applicationId + " not found"));
        }

        return modelMapper.map(passApplication, PassApplicationResponseDto.class);

    }


    private String generateApplicationNumber() {
        return "APP-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}
