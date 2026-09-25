package com.porfolio.EPassSystemSpringboot.services;

import com.porfolio.EPassSystemSpringboot.enums.PassType;
import com.porfolio.EPassSystemSpringboot.enums.PassValidity;

import java.math.BigDecimal;

public interface PassPriceService {

    BigDecimal getPrice(PassType passType, PassValidity passValidity);

}
