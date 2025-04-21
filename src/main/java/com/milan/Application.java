package com.milan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // Enables auto-population of createdOn/updatedOn
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
