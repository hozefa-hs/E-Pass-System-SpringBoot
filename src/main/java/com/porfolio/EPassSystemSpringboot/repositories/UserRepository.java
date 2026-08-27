package com.porfolio.EPassSystemSpringboot.repositories;

import com.porfolio.EPassSystemSpringboot.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByUsername(String username);

}
