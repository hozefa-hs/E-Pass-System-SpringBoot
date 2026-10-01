package com.porfolio.EPassSystemSpringboot.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RazorpayPaymentVerificationRequestDto {

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

}