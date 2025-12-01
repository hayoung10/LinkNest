package com.linknest.backend.storage;

import org.springframework.web.multipart.MultipartFile;

public interface Storage {
    String upload(String directory, MultipartFile file);
    void delete(String url);
}
