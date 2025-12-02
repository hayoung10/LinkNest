package com.linknest.backend.storage.local;

import com.linknest.backend.storage.Storage;
import com.linknest.backend.storage.StorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

            log.info("Storage: local upload success. directory={}, filename={}, url={}",
                    directory, originalFilename, url);
            return url;
        } catch (IOException e) {
            log.error("Storage: local upload failed. directory={}, originalFilename={}, reason={}",
                    directory, originalFilename, e.getMessage(), e);
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }

    @Override
    public void delete(String url) {
        if(url == null || url.isBlank()) return;

        try {
            String prefix = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
            // 상대 경로 추출 (URL에서 baseUrl 제거)
            String relativePath = url.replaceFirst("^" + java.util.regex.Pattern.quote(prefix), "");

            Path filePath = Paths.get(basePath, relativePath).toAbsolutePath().normalize();
            boolean deleted = Files.deleteIfExists(filePath);

            if(deleted) {
                log.info("Storage: local file deleted. url={}, path={}", url, filePath);
            } else {
                log.info("Storage: local file not found for delete. url={}, path={}", url, filePath);
            }
        } catch (IOException e) {
            log.warn("Storage: local file delete failed, reason={}", url, e.getMessage(), e);
        }
    }
}
