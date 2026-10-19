package com.porfolio.EPassSystemSpringboot.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PassValidationRequestDto {

    @NotBlank(message = "QR token is required")
    private String qrToken;

}