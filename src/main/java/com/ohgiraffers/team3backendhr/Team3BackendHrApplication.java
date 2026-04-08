package com.ohgiraffers.team3backendhr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.ohgiraffers.team3backendhr.infrastructure.client.feign")
public class Team3BackendHrApplication {

    public static void main(String[] args) {
        SpringApplication.run(Team3BackendHrApplication.class, args);
    }

}