package com.porfolio.EPassSystemSpringboot.tools;

import com.porfolio.EPassSystemSpringboot.enums.PassType;
import com.porfolio.EPassSystemSpringboot.enums.PassValidity;
import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PassPriceToolResult {

    private PassType passType;
    private PassValidity passValidity;
    private BigDecimal amount;
    private String currency;
    private boolean available;

}
