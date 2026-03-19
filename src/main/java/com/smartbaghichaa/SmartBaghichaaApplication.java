package com.smartbaghichaa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SmartBaghichaaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartBaghichaaApplication.class, args);
    }
}
