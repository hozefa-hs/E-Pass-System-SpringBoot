package com.porfolio.EPassSystemSpringboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EPassSystemSpringbootApplication {

	public static void main(String[] args) {
		SpringApplication.run(EPassSystemSpringbootApplication.class, args);
	}

}
