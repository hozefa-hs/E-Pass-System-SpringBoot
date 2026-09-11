package com.porfolio.EPassSystemSpringboot.services;


import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileStorageService {

    String uploadFile(MultipartFile file) throws IOException;

    String getPresignedUrl(String objectKey);

    void deleteFile(String fileUrl);
}
