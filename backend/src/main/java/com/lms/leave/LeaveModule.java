package com.lms.leave;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Leave Module Configuration
 * Similar to @Module() in NestJS
 * 
 * Represents the Leave module with all its components
 */
@Configuration
@ComponentScan(basePackages = "com.lms.leave")
public class LeaveModule {
    // This class represents the Leave module
    // All components (Controller, Service, Repository) are automatically registered
}
