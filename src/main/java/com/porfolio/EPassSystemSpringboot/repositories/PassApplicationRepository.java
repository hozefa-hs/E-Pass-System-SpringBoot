package com.porfolio.EPassSystemSpringboot.repositories;

import com.porfolio.EPassSystemSpringboot.entities.PassApplication;
import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassApplicationRepository extends JpaRepository<PassApplication, Long> {


    boolean existsByPassengerAndApplicationStatus(Users user, ApplicationStatus applicationStatus);

}
