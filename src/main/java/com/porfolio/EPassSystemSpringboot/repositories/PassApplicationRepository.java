package com.porfolio.EPassSystemSpringboot.repositories;

import com.porfolio.EPassSystemSpringboot.entities.PassApplication;
import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PassApplicationRepository extends JpaRepository<PassApplication, Long> {


    boolean existsByPassengerAndApplicationStatus(Users user, ApplicationStatus applicationStatus);

    List<PassApplication> findAllByPassengerUserId(Long userId);

    Page<PassApplication> findAllByApplicationStatus(ApplicationStatus status, Pageable pageable);

    PassApplication findByApplicationIdAndPassengerUserId(Long applicationId, Long userId);

}
