package com.porfolio.EPassSystemSpringboot.controllers;

import com.porfolio.EPassSystemSpringboot.dtos.PassValidationRequestDto;
import com.porfolio.EPassSystemSpringboot.dtos.PassValidationResponseDto;
import com.porfolio.EPassSystemSpringboot.services.PassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ticket-checker")
@RequiredArgsConstructor
public class TicketCheckerController {

    private final PassService passService;

    @PreAuthorize("hasRole('TICKET_CHECKER')")
    @PostMapping("/validate-pass")
    public ResponseEntity<PassValidationResponseDto> validatePass(
            @Valid @RequestBody PassValidationRequestDto requestDto) {

        PassValidationResponseDto responseDto = passService.validatePass(requestDto.getQrToken());

        return ResponseEntity.ok(responseDto);
    }

}
