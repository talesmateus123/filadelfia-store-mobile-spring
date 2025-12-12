package com.filadelfia.store.filadelfiastore.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Database initialization component for handling schema migrations
 * and data consistency checks on application startup.
 * 
 * This component ensures that the discriminator column for JPA inheritance
 * is properly initialized for existing records.
 */
@Component
public class DatabaseInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private Environment environment;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeDatabase() {
        // Skip database initialization for test profile
        if (environment.acceptsProfiles("test")) {
            logger.info("Skipping database initialization for test profile");
            return;
        }
        
        logger.info("Starting database schema validation and initialization...");
        
        try {
            // Check if discriminator column exists and is properly configured
            ensureDiscriminatorColumnIntegrity();
            
            logger.info("Database initialization completed successfully");
            
        } catch (Exception e) {
            logger.error("Critical error during database initialization: {}", e.getMessage(), e);
            // This is a critical issue that should stop the application
            throw new RuntimeException("Database initialization failed - application cannot start safely", e);
        }
    }
    
    private void ensureDiscriminatorColumnIntegrity() {
        try {
            // First, check if we have any records with invalid discriminator values
            Integer invalidRecords = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE user_type IS NULL OR user_type = '' OR TRIM(user_type) = ''", 
                Integer.class
            );
            
            if (invalidRecords != null && invalidRecords > 0) {
                logger.warn("Found {} records with invalid discriminator values. Applying data migration...", invalidRecords);
                
                // Apply business logic-based correction
                int updatedRows = jdbcTemplate.update(
                    "UPDATE users SET user_type = CASE " +
                    "WHEN role = 'CUSTOMER' THEN 'CUSTOMER' " +
                    "ELSE 'USER' " +
                    "END " +
                    "WHERE user_type IS NULL OR user_type = '' OR TRIM(user_type) = ''"
                );
                
                logger.info("Successfully migrated discriminator values for {} records", updatedRows);
                
                // Verify the fix
                Integer remainingInvalid = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM users WHERE user_type IS NULL OR user_type = '' OR TRIM(user_type) = ''", 
                    Integer.class
                );
                
                if (remainingInvalid != null && remainingInvalid > 0) {
                    throw new RuntimeException("Failed to fix all discriminator values. Remaining invalid records: " + remainingInvalid);
                }
            } else {
                logger.debug("All discriminator values are valid - no migration needed");
            }
            
        } catch (Exception e) {
            logger.error("Failed to ensure discriminator column integrity", e);
            throw e;
        }
    }
}
