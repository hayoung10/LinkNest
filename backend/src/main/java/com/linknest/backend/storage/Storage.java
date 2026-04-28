package com.linknest.backend.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface Storage {
    String upload(String directory, MultipartFile file);
    String upload(String directory, InputStream inputStream, String contentType);
    void delete(String url);
}
