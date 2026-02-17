package com.nick.npp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class NppDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(NppDemoApplication.class, args);
    }
}
