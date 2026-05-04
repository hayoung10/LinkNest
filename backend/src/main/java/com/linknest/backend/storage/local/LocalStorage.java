package com.linknest.backend.storage.local;

import com.linknest.backend.storage.Storage;
import com.linknest.backend.storage.StorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class LocalStorage implements Storage {

    private final String basePath; // 로컬 파일 저장 베이스 경로
    private final String baseUrl; // 클라이언트 접근 베이스 URL

    public LocalStorage(StorageProperties.Local properites) {
        this.basePath = properites.getBasePath();
        this.baseUrl = properites.getBaseUrl();
    }

    @Override
    public String upload(String directory, MultipartFile file) {
        if(file == null || file.isEmpty()) {
            log.warn("Storage: upload called with empty file. directory={}", directory);
            throw new IllegalArgumentException("업로드할 파일이 비어있습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if(originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = UUID.randomUUID() + extension;

        try {
            // 파일 저장 경로: {basePath}/{directory}/{filename}
            Path dirPath = Paths.get(basePath, directory).toAbsolutePath().normalize();
            Files.createDirectories(dirPath);

            Path target = dirPath.resolve(filename);
            file.transferTo(target.toFile());

            // URL용 경로: {baseUrl}/{directory}/{filename}
            String urlPath = directory + "/" + filename;
            String url = baseUrl.endsWith("/") ? baseUrl + urlPath : baseUrl + "/" + urlPath;

            log.debug("Storage: local upload success. directory={}, url={}", directory, url);
            return url;
        } catch (IOException e) {
            log.error("Storage: local upload failed. directory={}, originalFilename={}",
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

        String extension = extractExtension(contentType);
        String filename = UUID.randomUUID() + extension;

        try {
            Path dirPath = Paths.get(basePath, directory).toAbsolutePath().normalize();
            Files.createDirectories(dirPath);

            Path target = dirPath.resolve(filename);
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);

            // URL용 경로: {baseUrl}/{directory}/{filename}
            String urlPath = directory + "/" + filename;
            String url = baseUrl.endsWith("/") ? baseUrl + urlPath : baseUrl + "/" + urlPath;

            log.debug("Storage: local stream upload success. directory={}, url={}", directory, url);
            return url;
        } catch (IOException e) {
            log.error("Storage: local stream upload failed. directory={}", directory, e);
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }

    @Override
    public void delete(String url) {
        if(url == null || url.isBlank()) return;

        // 상대 경로 추출 (URL에서 baseUrl 제거)
        Optional<String> relativePathOptional = extractRelativePathFromUrl(url);
        if(relativePathOptional.isEmpty()) {
            log.info("Storage: skip local delete because url is not managed by current storage. url={}", url);
            return;
        }

        String relativePath = relativePathOptional.get();
        try {
            Path filePath = Paths.get(basePath, relativePath).toAbsolutePath().normalize();
            boolean deleted = Files.deleteIfExists(filePath);

            if(deleted) {
                log.debug("Storage: local file deleted. path={}", filePath);
            } else {
                log.debug("Storage: local file not found for delete. path={}", filePath);
            }
        } catch (IOException e) {
            log.warn("Storage: local file delete failed, url={}", url, e);
        }
    }

    private String extractExtension(String contentType) {
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

    private Optional<String> extractRelativePathFromUrl(String url) {
        String prefix = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";

        if(!url.startsWith(prefix)) {
            return Optional.empty();
        }

        return Optional.of(url.substring(prefix.length()));
    }
}
