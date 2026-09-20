package com.porfolio.EPassSystemSpringboot.services;

import com.porfolio.EPassSystemSpringboot.dtos.PassApplicationResponseDto;

public interface ReviewService {

    PassApplicationResponseDto approveApplication(Long applicationId);

    PassApplicationResponseDto rejectApplication(Long applicationId, String rejectionReason);

}
