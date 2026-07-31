package com.ntaganira.heritier.iVura;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IVuraApplication {

	private static final Logger log = LoggerFactory.getLogger(IVuraApplication.class);
	private static final String DEFAULT_PASSWORD = "password123";

	public static void main(String[] args) {
		SpringApplication.run(IVuraApplication.class, args);
		log.info("Default admin password: {}", DEFAULT_PASSWORD);
	}

}
