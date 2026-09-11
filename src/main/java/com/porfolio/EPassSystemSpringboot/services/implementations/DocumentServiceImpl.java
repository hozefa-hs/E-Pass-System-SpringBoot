package com.porfolio.EPassSystemSpringboot.services.implementations;

import com.porfolio.EPassSystemSpringboot.dtos.DocumentResponseDto;
import com.porfolio.EPassSystemSpringboot.dtos.UploadDocumentResponseDto;
import com.porfolio.EPassSystemSpringboot.entities.Document;
import com.porfolio.EPassSystemSpringboot.entities.PassApplication;
import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.enums.ApplicationStatus;
import com.porfolio.EPassSystemSpringboot.enums.DocumentType;
import com.porfolio.EPassSystemSpringboot.exceptions.BusinessException;
import com.porfolio.EPassSystemSpringboot.exceptions.ResourceNotFoundException;
import com.porfolio.EPassSystemSpringboot.repositories.DocumentRepository;
import com.porfolio.EPassSystemSpringboot.repositories.PassApplicationRepository;
import com.porfolio.EPassSystemSpringboot.repositories.UserRepository;
import com.porfolio.EPassSystemSpringboot.services.DocumentService;
import com.porfolio.EPassSystemSpringboot.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final FileStorageService fileStorageService;
    private final PassApplicationRepository passApplicationRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final ModelMapper modelMapper;


    @Override
    public UploadDocumentResponseDto uploadDocument(
            Long applicationId,
            MultipartFile file,
            DocumentType documentType,
            Long userId) throws IOException {

        //ownership check
        PassApplication passApplication = passApplicationRepository.findByApplicationIdAndPassengerUserId(applicationId, userId);

        //Application exists, but it does not belong to this passenger
        if (passApplication == null)
            throw new AccessDeniedException("You are not authorized to upload documents for this application");

        ApplicationStatus status = passApplication.getApplicationStatus();
        if (status != ApplicationStatus.PENDING && status != ApplicationStatus.REJECTED) {
            throw new BusinessException("Documents can only be uploaded for pending or rejected applications");
        }

        if (documentType == null) {
            throw new BusinessException("Document type is required");
        }

        //validate file
        validateFile(file);

        //upload file to S3
        String uploadedFile = fileStorageService.uploadFile(file);

        //Create Document entity
        Document document = new Document();
        document.setPassApplication(passApplication);
        document.setDocumentType(documentType);
        document.setFileName(file.getOriginalFilename());
        document.setFileUrl(uploadedFile);
        document.setContentType(file.getContentType());

        Document savedDocument;
        try {
            //Save to PostgreSQL
            savedDocument = documentRepository.save(document);
        }
        catch (Exception e) {
            fileStorageService.deleteFile(uploadedFile);
            throw e;
        }

        //convert to dto
        UploadDocumentResponseDto response = modelMapper.map(savedDocument, UploadDocumentResponseDto.class);

        response.setMessage("Document uploaded successfully");
        //Return UploadDocumentResponseDto
        return response;

    }


    @Override
    public List<DocumentResponseDto> getDocumentsByApplication(Long applicationId, Long userId) {
        return List.of();
    }

    @Override
    public void deleteDocument(Long documentId, Long userId) {

    }


    private void validateFile(MultipartFile file) {

        final long MAX_FILE_SIZE = 5 * 1024 * 1024; //5MB
        final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "png", "jpg", "jpeg");

        if (file == null || file.isEmpty()) {
            throw new ResourceNotFoundException("File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("File size must not exceed 5 MB.");
        }

        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName
                .substring(originalFileName.lastIndexOf('.') + 1)
                .toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Only PDF, PNG, JPG, and JPEG files are allowed.");
        }

    }
}
