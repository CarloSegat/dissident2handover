package com.dissident.issuer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({
	"com.dissident.common.service.*",
	"com.dissident.common.model.*",
})
@SpringBootApplication
public class IssuerApplication {
	public static void main(String[] args) {
		SpringApplication.run(IssuerApplication.class, args);
	}
}
