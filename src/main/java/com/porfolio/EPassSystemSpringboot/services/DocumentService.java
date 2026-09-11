package com.porfolio.EPassSystemSpringboot.services;

import com.porfolio.EPassSystemSpringboot.dtos.DocumentResponseDto;
import com.porfolio.EPassSystemSpringboot.dtos.UploadDocumentResponseDto;
import com.porfolio.EPassSystemSpringboot.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DocumentService {

    UploadDocumentResponseDto uploadDocument(Long applicationId, MultipartFile file, DocumentType documentType, Long userId) throws IOException;

    List<DocumentResponseDto> getDocumentsByApplication(Long applicationId, Long userId);

    void deleteDocument(Long documentId, Long userId);

}
