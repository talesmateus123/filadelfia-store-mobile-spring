package com.filadelfia.store.filadelfiastore.service.implementation;

import com.filadelfia.store.filadelfiastore.service.interfaces.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final String uploadDir;
    private final String baseUrl;
    
    // Allowed image types
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public LocalFileStorageService(
            @Value("${file.upload.dir:src/main/resources/static/images/products}") String uploadDir,
            @Value("${server.base-url:http://localhost:8080}") String baseUrl) {
        this.uploadDir = uploadDir;
        this.baseUrl = baseUrl;
        
        // Create upload directory if it doesn't exist
        try {
            Path path = Paths.get(uploadDir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadDir, e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String fileName) {
        // Validate file
        if (!isValidImage(file)) {
            throw new RuntimeException("Invalid image file");
        }

        try {
            // Clean filename and add UUID to prevent conflicts
            String cleanFileName = StringUtils.cleanPath(fileName);
            String extension = getFileExtension(cleanFileName);
            String uniqueFileName = UUID.randomUUID().toString() + "_" + cleanFileName;
            
            // Ensure extension is lowercase
            if (extension != null && !extension.isEmpty()) {
                uniqueFileName = uniqueFileName.substring(0, uniqueFileName.lastIndexOf('.')) + 
                               "." + extension.toLowerCase();
            }

            // Copy file to upload directory
            Path targetLocation = Paths.get(uploadDir).resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return getFileUrl(uniqueFileName);
            
        } catch (IOException e) {
            throw new RuntimeException("Could not store file " + fileName, e);
        }
    }

    @Override
    public boolean deleteFile(String fileName) {
        try {
            // Extract filename from URL if needed
            String actualFileName = extractFileNameFromUrl(fileName);
            Path filePath = Paths.get(uploadDir).resolve(actualFileName);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean fileExists(String fileName) {
        String actualFileName = extractFileNameFromUrl(fileName);
        Path filePath = Paths.get(uploadDir).resolve(actualFileName);
        return Files.exists(filePath);
    }

    @Override
    public String getFileUrl(String fileName) {
        return baseUrl + "/images/products/" + fileName;
    }

    @Override
    public boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            return false;
        }

        // Check MIME type
        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
            return false;
        }

        // Check file extension
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return false;
        }

        String extension = getFileExtension(fileName);
        return extension != null && ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    private String extractFileNameFromUrl(String fileUrl) {
        if (fileUrl == null) {
            return null;
        }
        
        // If it's already just a filename, return as is
        if (!fileUrl.contains("/")) {
            return fileUrl;
        }
        
        // Extract filename from URL
        return fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
    }
}
