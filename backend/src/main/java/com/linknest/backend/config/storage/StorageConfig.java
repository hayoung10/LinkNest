package com.linknest.backend.config.storage;

import com.linknest.backend.storage.Storage;
import com.linknest.backend.storage.StorageProperties;
import com.linknest.backend.storage.local.LocalStorage;
import com.linknest.backend.storage.s3.S3Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
@RequiredArgsConstructor
public class StorageConfig {
    private final StorageProperties storageProperties;

    @Bean
    public Storage storage() {
        StorageProperties.StorageProvider provider = storageProperties.getProvider();

        return switch (provider) {
            case LOCAL -> new LocalStorage(storageProperties.getLocal());
            case S3 -> new S3Storage(storageProperties.getS3());
        };
    }
}
