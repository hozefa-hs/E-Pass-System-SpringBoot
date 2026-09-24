package com.porfolio.EPassSystemSpringboot.services;

import com.porfolio.EPassSystemSpringboot.dtos.PassResponseDto;

public interface PassService {

    PassResponseDto issuePass(Long applicationId);

}
