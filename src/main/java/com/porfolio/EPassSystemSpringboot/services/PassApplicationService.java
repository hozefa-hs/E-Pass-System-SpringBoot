package com.porfolio.EPassSystemSpringboot.services;

import com.porfolio.EPassSystemSpringboot.dtos.CreatePassApplicationDto;
import com.porfolio.EPassSystemSpringboot.dtos.PassApplicationResponseDto;
import com.porfolio.EPassSystemSpringboot.dtos.ResubmitPassApplicationRequestDto;
import com.porfolio.EPassSystemSpringboot.entities.PassApplication;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PassApplicationService {

    PassApplicationResponseDto createApplication(CreatePassApplicationDto createPassApplicationDto, Long userId);

    List<PassApplicationResponseDto> getOwnApplications(Long userId);

    Page<PassApplicationResponseDto> getPendingApplications(int page, int size);

    PassApplicationResponseDto getApplicationById(Long applicationId, Long userId);

    PassApplicationResponseDto resubmitApplication(Long applicationId, Long passengerUserId, ResubmitPassApplicationRequestDto requestDto);

}
