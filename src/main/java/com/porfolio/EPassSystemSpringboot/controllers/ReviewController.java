package com.porfolio.EPassSystemSpringboot.controllers;

import com.porfolio.EPassSystemSpringboot.dtos.PassApplicationResponseDto;
import com.porfolio.EPassSystemSpringboot.dtos.ReviewApplicationDto;
import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.services.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/approve/{applicationId}")
    @PreAuthorize("hasRole('PASS_OFFICER')")
    public ResponseEntity<PassApplicationResponseDto> approveApplication(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal Users user) {

        PassApplicationResponseDto approvedApplication = reviewService.approveApplication(applicationId, user.getUserId());
        return ResponseEntity.ok(approvedApplication);
    }

    @PostMapping("/reject/{applicationId}")
    @PreAuthorize("hasRole('PASS_OFFICER')")
    public ResponseEntity<PassApplicationResponseDto> rejectApplication(
            @PathVariable Long applicationId,
            @Valid @RequestBody ReviewApplicationDto reviewApplicationDto,
            @AuthenticationPrincipal Users user) {

        PassApplicationResponseDto rejectedApplication = reviewService.rejectApplication(applicationId, user.getUserId(), reviewApplicationDto.getRejectionReason());
        return ResponseEntity.ok(rejectedApplication);
    }

}
