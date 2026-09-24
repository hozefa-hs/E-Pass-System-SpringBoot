package com.porfolio.EPassSystemSpringboot.repositories;

import com.porfolio.EPassSystemSpringboot.entities.Pass;
import com.porfolio.EPassSystemSpringboot.entities.PassApplication;
import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.enums.PassStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PassRepository extends JpaRepository<Pass, Long> {

    boolean existsByUserAndPassStatus(Users user, PassStatus passStatus);

    boolean existsByPassApplication(PassApplication passApplication);

    Optional<Pass> findByPassApplication(PassApplication passApplication);

}
