package com.vres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ComponentScan(basePackages = "com.vres")
@EntityScan(basePackages = "com.vres.entity")
@EnableJpaRepositories(basePackages = "com.vres.repository")
@EnableTransactionManagement
public class VresApplication {

	public static void main(String[] args) {
		SpringApplication.run(VresApplication.class, args);
	}

}
