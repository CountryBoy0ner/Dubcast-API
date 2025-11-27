package com.Tsimur.Dubcast;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DubcastApplication {
	public static void main(String[] args) {
		SpringApplication.run(DubcastApplication.class, args);
	}
}
