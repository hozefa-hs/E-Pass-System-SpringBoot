package com.porfolio.EPassSystemSpringboot.repositories;

import com.porfolio.EPassSystemSpringboot.entities.PassPrice;
import com.porfolio.EPassSystemSpringboot.enums.PassType;
import com.porfolio.EPassSystemSpringboot.enums.PassValidity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PassPriceRepository extends JpaRepository<PassPrice, Long> {

    Optional<PassPrice> findByPassTypeAndPassValidity(PassType passType, PassValidity passValidity);

}
