package com.porfolio.EPassSystemSpringboot.services;

import com.porfolio.EPassSystemSpringboot.dtos.CreatePassApplicationDto;
import com.porfolio.EPassSystemSpringboot.dtos.PassApplicationResponseDto;

public interface PassApplicationService {

    PassApplicationResponseDto createApplication(CreatePassApplicationDto createPassApplicationDto, Long userId);

}
