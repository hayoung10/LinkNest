package com.linknest.backend.config.web;

import com.linknest.backend.storage.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final StorageProperties storageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // LOCAL 스토리지일 때만 정적 파일 매핑
        if(storageProperties.getProvider() == StorageProperties.StorageProvider.LOCAL) {
            String basePath = storageProperties.getLocal().getBasePath();

            String location = "file:" + basePath + "/";

            registry.addResourceHandler("/files/**")
                    .addResourceLocations(location);
        }
    }
}
