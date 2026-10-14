package com.porfolio.EPassSystemSpringboot.services.implementations;

import com.porfolio.EPassSystemSpringboot.enums.PassStatus;
import com.porfolio.EPassSystemSpringboot.repositories.PassRepository;
import com.porfolio.EPassSystemSpringboot.services.PassExpirationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PassExpirationServiceImpl implements PassExpirationService {

    private final PassRepository passRepository;

    @Override
    public int expireExpiredPasses(LocalDate today) {

        return passRepository.expireActivePasses(PassStatus.ACTIVE, PassStatus.EXPIRED, today);
    }
}
