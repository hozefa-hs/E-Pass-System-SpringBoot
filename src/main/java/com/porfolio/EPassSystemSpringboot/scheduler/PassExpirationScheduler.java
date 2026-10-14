package com.porfolio.EPassSystemSpringboot.scheduler;

import com.porfolio.EPassSystemSpringboot.services.PassExpirationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class PassExpirationScheduler {

    private final PassExpirationService passExpirationService;
    private final Clock businessClock;


    //By default, this runs every day at midnight India time.
    @Scheduled(
            cron = "${pass-expiration.cron}",
            zone = "${business-time-zone}"
    )
    public void expirePasses() {

        LocalDate today = LocalDate.now(businessClock);

        int expiredCount = passExpirationService.expireExpiredPasses(today);

        if(expiredCount > 0) {
            log.info("Expired {} pass(es) on {}", expiredCount, today);
        }

    }

}
