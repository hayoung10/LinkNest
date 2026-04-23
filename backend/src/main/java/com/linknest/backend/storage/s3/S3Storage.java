package com.linknest.backend.storage.s3;

import com.linknest.backend.storage.Storage;
import com.linknest.backend.storage.StorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

@Slf4j
public class S3Storage implements Storage {
    private final String bucket;
    private final String baseUrl;
    private final String region;
    private final S3Client s3Client;

    public S3Storage(StorageProperties.S3 properties, S3Client s3Client) {
        this.bucket = properties.getBucket();
        this.baseUrl = properties.getBaseUrl();
        this.region = properties.getRegion();
        this.s3Client = s3Client;
    }

    @Override
    public String upload(String directory, MultipartFile file) {
        if(file == null || file.isEmpty()) {
            log.warn("Storage: upload called with empty file. directory={}", directory);
            throw new IllegalArgumentException("업로드할 파일이 비어있습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename, file.getContentType());
        String filename = UUID.randomUUID() + extension;
        String key = buildKey(directory, filename);

        try(InputStream inputStream = file.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));

            String url = buildUrl(key);
            log.debug("Storage: S3 upload success. directory={}, key={}", directory, key);
            return url;
        } catch (IOException e) {
            log.error("Storage: S3 upload failed. directory={}, originalFilename={}",
                    directory, originalFilename, e);
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }

    @Override
    public String upload(String directory, InputStream inputStream, String contentType) {
        if(inputStream == null) {
            log.warn("Storage: stream upload called with null inputStream. directory={}", directory);
            throw new IllegalArgumentException("업로드할 파일이 비어있습니다.");
        }

        String extension = extractExtension(null, contentType);
        String filename = UUID.randomUUID() + extension;
        String key = buildKey(directory, filename);

        try {
            byte[] bytes = inputStream.readAllBytes();

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(bytes));

            String url = buildUrl(key);
            log.debug("Storage: S3 stream upload success. directory={}, key={}", directory, key);
            return url;
        } catch (IOException e) {
            log.error("Storage: S3 stream upload failed. directory={}", directory, e);
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }

    @Override
    public void delete(String url) {
        if(url == null || url.isBlank()) return;

        String key = extractKeyFromUrl(url);
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
            log.info("Storage: S3 delete success. key={}", key);
        } catch (Exception e) {
            log.warn("Storage: S3 delete failed, key={}", key, e);
        }
    }

    private String buildKey(String directory, String filename) {
        String normalizedDirectory = directory == null ? "" : directory.strip();
        if(normalizedDirectory.isBlank()) {
            return filename;
        }
        return  normalizedDirectory.endsWith("/")
                ? normalizedDirectory + filename : normalizedDirectory + "/" + filename;
    }

    private String buildUrl(String key) {
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return  normalizedBaseUrl + "/" + key;
    }

    private String extractKeyFromUrl(String url) {
        String prefix = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        if(!url.startsWith(prefix)) {
            throw new IllegalArgumentException("S3 URL 형식이 올바르지 않습니다.");
        }
        return url.substring(prefix.length());
    }

    private String extractExtension(String originalFilename, String contentType) {
        if(originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase(Locale.ROOT);
        }

        if(contentType == null || contentType.isBlank()) {
            return "";
        }

        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }
}
