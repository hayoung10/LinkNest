package com.linknest.backend.config.storage;

import com.linknest.backend.storage.Storage;
import com.linknest.backend.storage.StorageProperties;
import com.linknest.backend.storage.local.LocalStorage;
import com.linknest.backend.storage.s3.S3Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
@RequiredArgsConstructor
public class StorageConfig {
    private final StorageProperties storageProperties;

    @Bean
    @ConditionalOnProperty(prefix = "storage", name = "provider", havingValue = "LOCAL", matchIfMissing = true)
    public Storage localStorage() {
        return new LocalStorage(storageProperties.getLocal());
    }

    @Bean
    @ConditionalOnProperty(prefix = "storage", name = "provider", havingValue = "S3")
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(storageProperties.getS3().getRegion()))
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "storage", name = "provider", havingValue = "S3")
    public Storage s3Storage(S3Client s3Client) {
        return new S3Storage(storageProperties.getS3(), s3Client);
    }
}
