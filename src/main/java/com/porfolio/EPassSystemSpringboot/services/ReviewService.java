package com.porfolio.EPassSystemSpringboot.services;

import com.porfolio.EPassSystemSpringboot.dtos.PassApplicationResponseDto;

public interface ReviewService {

    PassApplicationResponseDto approveApplication(Long applicationId, Long passOfficerId);

    PassApplicationResponseDto rejectApplication(Long applicationId, Long passOfficerId, String rejectionReason);

}
