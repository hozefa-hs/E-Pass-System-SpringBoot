package com.porfolio.EPassSystemSpringboot.services;

import java.time.LocalDate;

public interface PassExpirationService {

    int expireExpiredPasses(LocalDate today);

}
