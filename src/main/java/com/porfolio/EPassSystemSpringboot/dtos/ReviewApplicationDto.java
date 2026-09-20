package com.porfolio.EPassSystemSpringboot.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewApplicationDto {

    @NotBlank(message = "Rejection reason is required")
    @Size(max = 500, message = "Rejection reason must not exceed 500 characters")
    private String rejectionReason;

}
