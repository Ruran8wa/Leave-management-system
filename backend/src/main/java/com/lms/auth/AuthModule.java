package com.lms.auth;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Auth Module Configuration
 * Similar to @Module() in NestJS
 * 
 * In Spring Boot, @Configuration classes define beans and configurations
 * @ComponentScan ensures all components in this package are scanned
 * 
 * Note: Since we're using @SpringBootApplication in LmsApplication,
 * it automatically scans all sub-packages, so this is optional but
 * included for clarity and NestJS similarity
 */
@Configuration
@ComponentScan(basePackages = "com.lms.auth")
public class AuthModule {
    // This class represents the Auth module
    // All components (Controller, Service, Repository) are automatically registered
}
