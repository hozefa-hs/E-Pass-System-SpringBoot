package com.porfolio.EPassSystemSpringboot.repositories;

import com.porfolio.EPassSystemSpringboot.entities.Pass;
import com.porfolio.EPassSystemSpringboot.entities.PassApplication;
import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.enums.PassStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface PassRepository extends JpaRepository<Pass, Long> {

    boolean existsByUserAndPassStatus(Users user, PassStatus passStatus);

    boolean existsByPassApplication(PassApplication passApplication);

    Optional<Pass> findByPassApplication(PassApplication passApplication);

    boolean existsByUserAndPassStatusAndValidUntilGreaterThanEqual(Users user, PassStatus passStatus, LocalDate today);



    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Pass p
        SET p.passStatus = :expiredStatus
        WHERE p.passStatus = :activeStatus
          AND p.validUntil < :today
        """)
    int expireActivePasses(
            @Param("activeStatus") PassStatus activeStatus,
            @Param("expiredStatus") PassStatus expiredStatus,
            @Param("today") LocalDate today
    );

}
