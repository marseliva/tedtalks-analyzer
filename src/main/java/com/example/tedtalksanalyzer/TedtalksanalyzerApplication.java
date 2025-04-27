package com.example.tedtalksanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TedtalksanalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TedtalksanalyzerApplication.class, args);
    }

}
