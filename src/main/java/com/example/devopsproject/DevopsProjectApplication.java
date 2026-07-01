package com.example.devopsproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the DevOps GitOps Showcase Spring Boot Microservice.
 * This application serves a premium, dynamic web dashboard demonstrating
 * real-time containerized deployment information on Kubernetes.
 *
 * @author DevOps Engineer
 * @version 1.0.0
 */
@SpringBootApplication
public class DevopsProjectApplication {

    /**
     * Main method to bootstrap and run the Spring Boot application.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(DevopsProjectApplication.class, args);
    }
}
