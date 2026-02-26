package com.filadelfia.store.filadelfiastore.service.implementation;

import com.filadelfia.store.filadelfiastore.service.interfaces.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * AWS S3 File Storage Service Implementation
 * 
 * This service will be enabled when file.storage.type=s3 is set in application.properties
 * 
 * To use this service, add the following dependencies to pom.xml:
 * 
 * <dependency>
 *     <groupId>software.amazon.awssdk</groupId>
 *     <artifactId>s3</artifactId>
 * </dependency>
 * 
 * And configure the following properties:
 * file.storage.type=s3
 * aws.s3.bucket-name=your-bucket-name
 * aws.s3.region=us-east-1
 * aws.access-key-id=your-access-key
 * aws.secret-access-key=your-secret-key
 */
@Service
@ConditionalOnProperty(name = "file.storage.type", havingValue = "s3")
public class S3FileStorageService implements FileStorageService {

    // Allowed image types
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final String bucketName;
    private final String region;
    private final String cdnUrl;

    public S3FileStorageService(
            @Value("${aws.s3.bucket-name}") String bucketName,
            @Value("${aws.s3.region:us-east-1}") String region,
            @Value("${aws.s3.cdn-url:}") String cdnUrl) {
        this.bucketName = bucketName;
        this.region = region;
        this.cdnUrl = cdnUrl;
    }

    @Override
    public String storeFile(MultipartFile file, String fileName) {
        // TODO: Implement S3 upload
        // This is a placeholder for future S3 implementation
        
        throw new UnsupportedOperationException(
            "S3 File Storage is not yet implemented. " +
            "Add AWS SDK dependencies and implement this method to use S3 storage."
        );
    }

    @Override
    public boolean deleteFile(String fileName) {
        // TODO: Implement S3 delete
        throw new UnsupportedOperationException("S3 File Storage is not yet implemented.");
    }

    @Override
    public boolean fileExists(String fileName) {
        // TODO: Implement S3 file existence check
        throw new UnsupportedOperationException("S3 File Storage is not yet implemented.");
    }

    @Override
    public String getFileUrl(String fileName) {
        if (cdnUrl != null && !cdnUrl.isEmpty()) {
            return cdnUrl + "/" + fileName;
        }
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
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

    /*
     * Future S3 Implementation Example:
     * 
     * @Autowired
     * private S3Client s3Client;
     * 
     * @Override
     * public String storeFile(MultipartFile file, String fileName) {
     *     if (!isValidImage(file)) {
     *         throw new RuntimeException("Invalid image file");
     *     }
     * 
     *     try {
     *         String uniqueFileName = "products/" + UUID.randomUUID().toString() + "_" + fileName;
     *         
     *         PutObjectRequest putObjectRequest = PutObjectRequest.builder()
     *             .bucket(bucketName)
     *             .key(uniqueFileName)
     *             .contentType(file.getContentType())
     *             .contentLength(file.getSize())
     *             .build();
     * 
     *         s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
     *         
     *         return getFileUrl(uniqueFileName);
     *         
     *     } catch (Exception e) {
     *         throw new RuntimeException("Could not store file in S3: " + fileName, e);
     *     }
     * }
     */
}
