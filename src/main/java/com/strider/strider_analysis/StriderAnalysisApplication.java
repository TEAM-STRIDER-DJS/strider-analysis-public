package com.strider.strider_analysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.strider.strider_analysis.infra")
public class StriderAnalysisApplication {

	public static void main(String[] args) {
		SpringApplication.run(StriderAnalysisApplication.class, args);
	}

}
