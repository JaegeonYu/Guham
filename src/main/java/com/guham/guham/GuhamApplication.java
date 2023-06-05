package com.guham.guham;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
public class GuhamApplication {

    public static void main(String[] args) {
       SpringApplication.run(GuhamApplication.class, args);
    }

}
