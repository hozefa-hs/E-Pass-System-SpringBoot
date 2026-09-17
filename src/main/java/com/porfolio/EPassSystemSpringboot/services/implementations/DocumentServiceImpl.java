package com.porfolio.EPassSystemSpringboot.services.implementations;

import com.porfolio.EPassSystemSpringboot.dtos.DocumentResponseDto;
import com.porfolio.EPassSystemSpringboot.dtos.UploadDocumentResponseDto;
import com.porfolio.EPassSystemSpringboot.entities.Document;
import com.porfolio.EPassSystemSpringboot.entities.PassApplication;
import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.enums.ApplicationStatus;
import com.porfolio.EPassSystemSpringboot.enums.DocumentType;
import com.porfolio.EPassSystemSpringboot.enums.Role;
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
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final FileStorageService fileStorageService;
    private final PassApplicationRepository passApplicationRepository;
    private final DocumentRepository documentRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;


    @Override
    public UploadDocumentResponseDto uploadDocument(
            Long applicationId,
            MultipartFile file,
            DocumentType documentType,
            Long userId) throws IOException {

        //uploadDocument()
        //    ↓
        //document exists?
        //    ├── No  → create
        //    └── Yes → replace

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


        //Check whether this document type already exists in pass application
        Optional<Document> existingDocument = documentRepository.findByPassApplicationApplicationIdAndDocumentType(applicationId, documentType);


        String oldFileUrl = existingDocument.map(Document::getStorageKey).orElse(null);


        //upload new file to S3
        String uploadedFile = fileStorageService.uploadFile(file, applicationId);


        Document document;

        if (existingDocument.isPresent()) {

            // Existing document -> replace metadata
            document = existingDocument.get();

            document.setFileName(file.getOriginalFilename());
            document.setStorageKey(uploadedFile);
            document.setContentType(file.getContentType());
        } else {

            // First upload -> create new document
            document = new Document();

            document.setPassApplication(passApplication);
            document.setDocumentType(documentType);
            document.setFileName(file.getOriginalFilename());
            document.setStorageKey(uploadedFile);
            document.setContentType(file.getContentType());
        }

        Document savedDocument;
        try {

            //Save database changes
            savedDocument = documentRepository.save(document);
        } catch (RuntimeException e) {

            // DB failed -> delete newly uploaded S3 object
            fileStorageService.deleteFile(uploadedFile);
            throw e;
        }


        //Delete old S3 object only after DB operation succeeds
        if (oldFileUrl != null && !oldFileUrl.equals(uploadedFile)) {
            fileStorageService.deleteFile(oldFileUrl);
        }


        //convert to dto
        UploadDocumentResponseDto uploadDocumentResponseDto = modelMapper.map(savedDocument, UploadDocumentResponseDto.class);

        uploadDocumentResponseDto.setFileUrl(fileStorageService.getPresignedUrl(savedDocument.getStorageKey()));

        uploadDocumentResponseDto.setMessage(
                existingDocument.isPresent()
                        ? "Document replaced successfully"
                        : "Document uploaded successfully"
        );

        return uploadDocumentResponseDto;

    }


    //First time upload scenario
/*
    Application #10     //Pass application id
    STUDENT_ID          //Document type
       ↓
    new S3 object A
       ↓
    new Document row A
*/

    /*---------------------------------------------*/

    //Corrected upload scenario
/*

    Application #10
    STUDENT_ID
       ↓
    S3 object B
       ↓
    update Document row A
       ↓
    delete S3 object A

*/


    @Override
    public List<DocumentResponseDto> getDocumentsByApplication(Long applicationId, Long userId) {

        Users user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        //These conditions satisfy that when the user is passenger. they can only see their own documents(ownership check)
        //and when the user is Pass Officer. He can see any application's documents.
        List<Document> documentList;
        if(user.getRole() == Role.PASSENGER) {

            // Verify application belongs to the passenger
            PassApplication passApplication = passApplicationRepository.findByApplicationIdAndPassengerUserId(applicationId, userId);

            if(passApplication == null) {
                throw new AccessDeniedException("You are not authorized to view documents for this application");
            }

            documentList = documentRepository.findAllByPassApplicationApplicationId(applicationId);

        }
        else if (user.getRole() == Role.PASS_OFFICER) {
            documentList = documentRepository.findAllByPassApplicationApplicationId(applicationId);
        }
        else {
            throw new AccessDeniedException("You are not authorized to view this documents");
        }

        return documentList
                .stream()
                .map(document -> {

                    DocumentResponseDto response = modelMapper.map(document, DocumentResponseDto.class);

                    // Generate a fresh temporary URL for the private S3 object
                    String presignedUrl = fileStorageService.getPresignedUrl(document.getStorageKey());

                    response.setFileUrl(presignedUrl);

                    return response;

                })
                .toList();
    }

    @Override
    public void deleteDocument(Long documentId, Long userId) {
        if (documentId == null ) {
            throw new ResourceNotFoundException("Document id not found while deleting document");
        }

        Optional<Document> document = documentRepository.findByDocumentIdAndPassApplicationPassengerUserId(documentId, userId);
        if (document.isEmpty()) {
            throw new AccessDeniedException("You are not authorized to delete this document");
        }

        ApplicationStatus status = document.get().getPassApplication().getApplicationStatus();
        if(status != ApplicationStatus.PENDING && status != ApplicationStatus.REJECTED) {
            throw new BusinessException("Document cannot be deleted for approved applications");
        }

        String fileUrl = document.get().getStorageKey();

        documentRepository.deleteById(documentId);
        fileStorageService.deleteFile(fileUrl);
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
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new BusinessException("File name is required");
        }
        String extension = originalFileName
                .substring(originalFileName.lastIndexOf('.') + 1)
                .toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Only PDF, PNG, JPG, and JPEG files are allowed.");
        }

    }
}
