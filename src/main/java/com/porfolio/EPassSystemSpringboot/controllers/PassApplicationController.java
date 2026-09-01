package com.porfolio.EPassSystemSpringboot.controllers;

import com.porfolio.EPassSystemSpringboot.dtos.CreatePassApplicationDto;
import com.porfolio.EPassSystemSpringboot.dtos.PassApplicationResponseDto;
import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.services.PassApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/application")
public class PassApplicationController {

    private final PassApplicationService passApplicationService;

    @PostMapping("/apply-pass")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<PassApplicationResponseDto> applyPass(
            @Valid @RequestBody CreatePassApplicationDto createPassApplicationDto,
            @AuthenticationPrincipal Users users) {

        PassApplicationResponseDto responseDto = passApplicationService.createApplication(createPassApplicationDto, users.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<List<PassApplicationResponseDto>> getOwnApplications(@AuthenticationPrincipal Users users) {
        List<PassApplicationResponseDto> passApplicationList = passApplicationService.getOwnApplications(users.getUserId());
        return ResponseEntity.ok(passApplicationList);
    }

    @GetMapping("/all-pending-applications")
    @PreAuthorize("hasRole('PASS_OFFICER')")
    public ResponseEntity<Page<PassApplicationResponseDto>> getAllPendingApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Page<PassApplicationResponseDto> pendingApplications = passApplicationService.getPendingApplications(page, size);
        return ResponseEntity.ok(pendingApplications);
    }

    @GetMapping("/applicationById/{applicationId}")
    @PreAuthorize("hasAnyRole('PASS_OFFICER', 'PASSENGER')")
    public ResponseEntity<PassApplicationResponseDto> getApplicationById(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal Users user) {
        PassApplicationResponseDto applicationById = passApplicationService.getApplicationById(applicationId, user.getUserId());
        return ResponseEntity.ok(applicationById);
    }


}
