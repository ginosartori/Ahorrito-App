package com.ahorrito.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AppAhorritoApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppAhorritoApplication.class, args);
	}

}
