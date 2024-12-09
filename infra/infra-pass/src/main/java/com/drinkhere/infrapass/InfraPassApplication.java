package com.drinkhere.infrapass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.drinkhere")
@ConfigurationPropertiesScan
public class InfraPassApplication {

    public static void main(String[] args) {
        SpringApplication.run(InfraPassApplication.class, args);
    }

}
