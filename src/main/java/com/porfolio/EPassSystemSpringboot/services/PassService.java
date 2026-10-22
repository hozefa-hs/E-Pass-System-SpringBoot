package com.porfolio.EPassSystemSpringboot.services;

import com.porfolio.EPassSystemSpringboot.dtos.PassResponseDto;
import com.porfolio.EPassSystemSpringboot.dtos.PassValidationResponseDto;

public interface PassService {

    PassResponseDto issuePass(Long applicationId);

    PassValidationResponseDto validatePass(String qrToken);

    byte[] generateActivePassQrCode(Long passengerUserId);

}
