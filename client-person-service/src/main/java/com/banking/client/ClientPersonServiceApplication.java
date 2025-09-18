package com.banking.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Client Person Service Application
 * Microservice for managing clients and persons
 */
@SpringBootApplication
@EnableFeignClients
public class ClientPersonServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientPersonServiceApplication.class, args);
    }
}
