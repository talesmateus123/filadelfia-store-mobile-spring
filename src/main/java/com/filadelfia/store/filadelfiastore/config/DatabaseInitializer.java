package com.filadelfia.store.filadelfiastore.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Database initialization component for handling schema migrations
 * and data consistency checks on application startup.
 * 
 * This component ensures that the User role column is properly configured
 * for existing records.
 */
@Component
public class DatabaseInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeDatabase() {
        logger.info("Starting database schema validation and initialization...");
        
        try {
            // Check if role column exists and is properly configured
            ensureRoleColumnIntegrity();
            
            logger.info("Database initialization completed successfully");
            
        } catch (Exception e) {
            logger.error("Critical error during database initialization: {}", e.getMessage(), e);
            // This is a critical issue that should stop the application
            throw new RuntimeException("Database initialization failed - application cannot start safely", e);
        }
    }
    
    private void ensureRoleColumnIntegrity() {
        try {
            // First, check if we have any records with invalid role values
            Integer invalidRecords = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE role IS NULL OR role = '' OR TRIM(role) = ''", 
                Integer.class
            );
            
            if (invalidRecords != null && invalidRecords > 0) {
                logger.warn("Found {} records with invalid role values. Applying data migration...", invalidRecords);
                
                // Apply business logic-based correction - set default role to USER
                int updatedRows = jdbcTemplate.update(
                    "UPDATE users SET role = 'USER' " +
                    "WHERE role IS NULL OR role = '' OR TRIM(role) = ''"
                );
                
                logger.info("Successfully migrated role values for {} records", updatedRows);
                
                // Verify the fix
                Integer remainingInvalid = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM users WHERE role IS NULL OR role = '' OR TRIM(role) = ''", 
                    Integer.class
                );
                
                if (remainingInvalid != null && remainingInvalid > 0) {
                    throw new RuntimeException("Failed to fix all role values. Remaining invalid records: " + remainingInvalid);
                }
            } else {
                logger.debug("All role values are valid - no migration needed");
            }
            
        } catch (Exception e) {
            logger.error("Failed to ensure role column integrity", e);
            throw e;
        }
    }
}
