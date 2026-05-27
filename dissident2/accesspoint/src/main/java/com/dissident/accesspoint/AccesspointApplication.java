package com.dissident.accesspoint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({
		"com.dissident.common.service",
		"com.dissident.common.model",
		"com.dissident.common.config",
		"com.dissident.accesspoint.service",
})
public class AccesspointApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccesspointApplication.class, args);
	}

}
