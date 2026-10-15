package com.porfolio.EPassSystemSpringboot.dtos;

import com.porfolio.EPassSystemSpringboot.enums.PassType;
import com.porfolio.EPassSystemSpringboot.enums.PassValidity;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResubmitPassApplicationRequestDto {

    @NotNull(message = "Pass type cannot be null")
    private PassType passType;

    @NotNull(message = "Pass validity cannot be null")
    private PassValidity passValidity;

}
