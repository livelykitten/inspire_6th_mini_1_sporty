package com.example.sporty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class SportyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SportyApplication.class, args);
	}

}
