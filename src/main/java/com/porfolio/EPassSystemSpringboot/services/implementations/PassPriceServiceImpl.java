package com.porfolio.EPassSystemSpringboot.services.implementations;

import com.porfolio.EPassSystemSpringboot.entities.PassPrice;
import com.porfolio.EPassSystemSpringboot.enums.PassType;
import com.porfolio.EPassSystemSpringboot.enums.PassValidity;
import com.porfolio.EPassSystemSpringboot.exceptions.BusinessException;
import com.porfolio.EPassSystemSpringboot.repositories.PassPriceRepository;
import com.porfolio.EPassSystemSpringboot.services.PassPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PassPriceServiceImpl implements PassPriceService {

    private final PassPriceRepository passPriceRepository;

    @Override
    public BigDecimal getPrice(PassType passType, PassValidity passValidity) {

        PassPrice passPrice = passPriceRepository.findByPassTypeAndPassValidity(passType, passValidity).orElseThrow(() -> new BusinessException("Price not configured for " + passType + " - " + passValidity));

        return passPrice.getAmount();

    }
}
