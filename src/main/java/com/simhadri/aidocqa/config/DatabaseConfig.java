// config/DatabaseConfig.java
package com.simhadri.aidocqa.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@Configuration
@EnableJpaRepositories(basePackages = "com.simhadri.aidocqa.repository")
@EntityScan(basePackages = "com.simhadri.aidocqa.model")
@EnableTransactionManagement
public class DatabaseConfig {
    // Database configuration is handled by Spring Boot auto-configuration
    // This class just enables JPA repositories and transaction management
}