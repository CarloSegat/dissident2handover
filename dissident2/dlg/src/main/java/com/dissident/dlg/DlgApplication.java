package com.dissident.dlg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({
	"com.dissident.common.service",
	"com.dissident.common.model", 
	"com.dissident.common.config", 
})
@SpringBootApplication
public class DlgApplication {

	public static void main(String[] args) {
		SpringApplication.run(DlgApplication.class, args);
	}

}
