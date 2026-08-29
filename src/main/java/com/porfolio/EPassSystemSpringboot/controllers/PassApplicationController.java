package com.porfolio.EPassSystemSpringboot.controllers;

import com.porfolio.EPassSystemSpringboot.dtos.CreatePassApplicationDto;
import com.porfolio.EPassSystemSpringboot.dtos.PassApplicationResponseDto;
import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.services.PassApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/passApplication")
public class PassApplicationController {

    private final PassApplicationService passApplicationService;

    @PostMapping("/apply-pass")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<PassApplicationResponseDto> applyPass(
            @Valid @RequestBody CreatePassApplicationDto createPassApplicationDto,
            @AuthenticationPrincipal Users users) {

        PassApplicationResponseDto responseDto = passApplicationService.createApplication(createPassApplicationDto, users.getUserId());
        return ResponseEntity.ok(responseDto);
    }

}
