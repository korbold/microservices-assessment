package com.banking.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Account Movement Service Application
 * Microservice for managing accounts and movements
 */
@SpringBootApplication
@EnableFeignClients
public class AccountMovementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountMovementServiceApplication.class, args);
    }
}
