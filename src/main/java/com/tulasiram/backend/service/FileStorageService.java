package com.tulasiram.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    // These values are injected from your application.properties file
    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    // Helper method to initialize the Cloudinary client
    private Cloudinary getCloudinary() {
        if (cloudinary == null) {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", this.cloudName,
                    "api_key", this.apiKey,
                    "api_secret", this.apiSecret,
                    "secure", true
            ));
        }
        return cloudinary;
    }

    public String uploadVideoFile(MultipartFile file) throws IOException {
        // We can add validation logic here later

        // Generate a unique public ID for the file to prevent overwrites
        String uniqueFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // Upload the file to Cloudinary
        Map<?, ?> uploadResult = getCloudinary().uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "resource_type", "video", // Specify that this is a video
                "public_id", uniqueFilename
        ));

        // Return the secure URL of the uploaded video
        return (String) uploadResult.get("secure_url");
    }
    public String uploadDocumentFile(MultipartFile file) throws IOException {
        String uniqueFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // For documents, we use the "raw" resource type in Cloudinary, which preserves the file as is.
        Map<?, ?> uploadResult = getCloudinary().uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "resource_type", "raw",
                "public_id", uniqueFilename
        ));
        return (String) uploadResult.get("secure_url");
    }
}
