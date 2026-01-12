package com.lms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Database Configuration
 * Enables JPA Auditing for automatic timestamp management
 */
@Configuration
@EnableJpaAuditing
public class DatabaseConfig {
    // Spring Data JPA will automatically handle database connections
    // based on application.properties configuration
}
