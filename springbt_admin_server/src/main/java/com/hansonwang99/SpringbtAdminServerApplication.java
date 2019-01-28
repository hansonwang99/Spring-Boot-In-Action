package com.hansonwang99;

import de.codecentric.boot.admin.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAdminServer
@SpringBootApplication
public class SpringbtAdminServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbtAdminServerApplication.class, args);
	}
}
