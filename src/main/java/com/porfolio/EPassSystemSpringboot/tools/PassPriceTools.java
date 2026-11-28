package com.porfolio.EPassSystemSpringboot.tools;


import com.porfolio.EPassSystemSpringboot.enums.PassType;
import com.porfolio.EPassSystemSpringboot.enums.PassValidity;
import com.porfolio.EPassSystemSpringboot.exceptions.BusinessException;
import com.porfolio.EPassSystemSpringboot.services.PassPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class PassPriceTools {

    private final PassPriceService passPriceService;

    @Tool(name = "getPassPrice", description = """
            Retrieve the actual configured price of an E-Pass bus pass
                    using its pass type and validity period.
                    Use this tool whenever a passenger asks about pass prices,
                    costs, or fees. Never guess a price.
            """)
    public PassPriceToolResult getPrice(@ToolParam(description = "Pass category: STUDENT, SENIOR_CITIZEN, or CORPORATE") PassType passType,

                                        @ToolParam(description = "Validity period: ONE_MONTH, THREE_MONTH, or SIX_MONTH") PassValidity passValidity) {

        try {

            log.info("AI price lookup: passType={}, validity={}", passType, passValidity);

            BigDecimal price = passPriceService.getPrice(passType, passValidity);

            return new PassPriceToolResult(passType, passValidity, price, "INR", true);

        } catch (BusinessException exception) {

            return new PassPriceToolResult(passType, passValidity, null, "INR", false);
        }

    }
}
