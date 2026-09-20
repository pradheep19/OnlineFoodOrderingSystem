package com.foodordering.system;

import com.foodordering.system.database.DatabaseManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OnlineFoodOrderingApplication {

    public static void main(String[] args) {

        DatabaseManager.initializeDatabase();

        SpringApplication.run(OnlineFoodOrderingApplication.class, args);
    }
}