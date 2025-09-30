package com.livewithoutthinking.resq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ResqBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResqBackendApplication.class, args);
	}

}
