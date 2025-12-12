package com.filadelfia.store.filadelfiastore.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    
    /**
     * Store a file and return the public URL to access it
     * 
     * @param file The file to store
     * @param fileName The desired filename (without path)
     * @return The public URL to access the stored file
     * @throws RuntimeException if storage fails
     */
    String storeFile(MultipartFile file, String fileName);
    
    /**
     * Delete a file by its filename
     * 
     * @param fileName The filename to delete
     * @return true if file was deleted, false if it didn't exist
     */
    boolean deleteFile(String fileName);
    
    /**
     * Check if a file exists
     * 
     * @param fileName The filename to check
     * @return true if file exists, false otherwise
     */
    boolean fileExists(String fileName);
    
    /**
     * Get the public URL for a filename
     * 
     * @param fileName The filename
     * @return The public URL to access the file
     */
    String getFileUrl(String fileName);
    
    /**
     * Validate if the file is a valid image
     * 
     * @param file The file to validate
     * @return true if valid image, false otherwise
     */
    boolean isValidImage(MultipartFile file);
}
