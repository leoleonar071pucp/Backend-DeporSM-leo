package com.example.deporsm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DeporsmApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeporsmApplication.class, args);
    }

}
