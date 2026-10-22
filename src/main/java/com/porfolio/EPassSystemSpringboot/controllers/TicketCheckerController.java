package com.porfolio.EPassSystemSpringboot.controllers;

import com.porfolio.EPassSystemSpringboot.dtos.PassValidationRequestDto;
import com.porfolio.EPassSystemSpringboot.dtos.PassValidationResponseDto;
import com.porfolio.EPassSystemSpringboot.exceptions.BusinessException;
import com.porfolio.EPassSystemSpringboot.services.PassService;
import com.porfolio.EPassSystemSpringboot.services.QrCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/ticket-checker")
@RequiredArgsConstructor
public class TicketCheckerController {

    private final PassService passService;
    private final QrCodeService qrCodeService;

    @PreAuthorize("hasRole('TICKET_CHECKER')")
    @PostMapping("/validate-pass")
    public ResponseEntity<PassValidationResponseDto> validatePass(
            @Valid @RequestBody PassValidationRequestDto requestDto) {

        PassValidationResponseDto responseDto = passService.validatePass(requestDto.getQrToken());

        return ResponseEntity.ok(responseDto);
    }


    @PostMapping(value = "/validate-qr-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TICKET_CHECKER')")
    public ResponseEntity<PassValidationResponseDto> validateQrImage(
            @RequestParam("qrImage") MultipartFile qrImage) {

        if (qrImage.isEmpty()) {
            throw new BusinessException("QR code image is required");
        }

        // Limit uploaded QR images to 2 MB.
        if (qrImage.getSize() > 2 * 1024 * 1024) {
            throw new BusinessException("QR code image must not exceed 2 MB");
        }

        byte[] imageBytes;

        try {
            imageBytes = qrImage.getBytes();
        } catch (IOException exception) {
            throw new BusinessException("Unable to read the uploaded QR code image");
        }

        String qrToken = qrCodeService.decodeQrCode(imageBytes);

        PassValidationResponseDto responseDto = passService.validatePass(qrToken);

        return ResponseEntity.ok(responseDto);

    }

}
