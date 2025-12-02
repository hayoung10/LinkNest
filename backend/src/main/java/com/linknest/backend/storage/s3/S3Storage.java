package com.linknest.backend.storage.s3;

import com.linknest.backend.storage.Storage;
import com.linknest.backend.storage.StorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
public class S3Storage implements Storage {
    private final String bucket;
    private final String baseUrl;
    private final String region;

    public S3Storage(StorageProperties.S3 properties) {
        this.bucket = properties.getBucket();
        this.baseUrl = properties.getBaseUrl();
        this.region = properties.getRegion();
    }

    @Override
    public String upload(String directory, MultipartFile file) {
        // TODO: S3 업로드 구현
        throw new UnsupportedOperationException("S3 upload not implemented yet.");
    }

    @Override
    public void delete(String url) {
        // TODO: S3 삭제 구현
        throw new UnsupportedOperationException("S3 delete not implemented yet.");
    }
}
