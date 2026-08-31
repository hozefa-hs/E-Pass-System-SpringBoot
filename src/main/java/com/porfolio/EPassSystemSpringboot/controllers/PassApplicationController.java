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
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/my-applications")
    public ResponseEntity<List<PassApplicationResponseDto>> getOwnApplications(@AuthenticationPrincipal Users users) {
        List<PassApplicationResponseDto> passApplicationList = passApplicationService.getOwnApplications(users.getUserId());
        return ResponseEntity.ok(passApplicationList);
    }

    @GetMapping("/all-pending-applications")
    @PreAuthorize("hasRole('PASS_OFFICER')")
    public ResponseEntity<List<PassApplicationResponseDto>> getAllPendingApplications() {
        List<PassApplicationResponseDto> pendingApplications = passApplicationService.getPendingApplications();
        return ResponseEntity.ok(pendingApplications);
    }

    @GetMapping("/applocationById/{applicationId}")
    @PreAuthorize("hasRole('PASS_OFFICER')")
    public ResponseEntity<PassApplicationResponseDto> getApplicationById(@PathVariable Long applicationId){
        PassApplicationResponseDto applicationById = passApplicationService.getApplicationById(applicationId);
        return ResponseEntity.ok(applicationById);
    }


}
