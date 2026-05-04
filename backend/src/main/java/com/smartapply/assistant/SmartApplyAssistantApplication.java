package com.smartapply.assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartApplyAssistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartApplyAssistantApplication.class, args);
	}

}
