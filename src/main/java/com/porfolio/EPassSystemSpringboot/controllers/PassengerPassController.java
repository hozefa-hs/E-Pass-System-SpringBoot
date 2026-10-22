package com.porfolio.EPassSystemSpringboot.controllers;

import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.services.PassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/passenger/pass")
public class PassengerPassController {

    private final PassService passService;

    @GetMapping(value = "/qr-code", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<byte[]> getMyActivePassQrCode(@AuthenticationPrincipal Users user) {

        byte[] passQrCode = passService.generateActivePassQrCode(user.getUserId());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.noStore())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"active-pass-qr.png\""
                )
                .body(passQrCode);
    }

}
