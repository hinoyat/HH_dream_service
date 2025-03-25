package com.c202.diary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan(basePackages = {"com.c202.*"})
public class DiaryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiaryServiceApplication.class, args);
	}

}
