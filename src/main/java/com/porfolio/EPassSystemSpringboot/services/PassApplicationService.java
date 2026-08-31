package com.porfolio.EPassSystemSpringboot.services;

import com.porfolio.EPassSystemSpringboot.dtos.CreatePassApplicationDto;
import com.porfolio.EPassSystemSpringboot.dtos.PassApplicationResponseDto;

import java.util.List;

public interface PassApplicationService {

    PassApplicationResponseDto createApplication(CreatePassApplicationDto createPassApplicationDto, Long userId);

    List<PassApplicationResponseDto> getOwnApplications(Long userId);

    List<PassApplicationResponseDto> getPendingApplications();

    PassApplicationResponseDto getApplicationById(Long applicationId);

}
