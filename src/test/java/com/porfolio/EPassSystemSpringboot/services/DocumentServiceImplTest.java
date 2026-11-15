package com.porfolio.EPassSystemSpringboot.services;

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
import com.porfolio.EPassSystemSpringboot.services.implementations.DocumentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    private static final Long APPLICATION_ID = 10L;
    private static final Long USER_ID = 20L;
    private static final Long DOCUMENT_ID = 30L;

    private static final String NEW_STORAGE_KEY = "documents/10/new-student-id.pdf";

    private static final String OLD_STORAGE_KEY = "documents/10/old-student-id.pdf";

    private static final String PRESIGNED_URL = "https://example.com/presigned";

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private PassApplicationRepository passApplicationRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DocumentServiceImpl documentService;


    // -------------------------------------------------------------------------
    // uploadDocument
    // -------------------------------------------------------------------------

    @Test
    void uploadDocument_applicationNotOwnedByPassenger_throwsAccessDenied() throws IOException {

        // Arrange
        MultipartFile file = mockFile();

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(null);

        // Act
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> documentService.uploadDocument(APPLICATION_ID, file, DocumentType.STUDENT_ID, USER_ID));

        // Assert
        assertEquals("You are not authorized to upload documents for this application", exception.getMessage());

        verifyNoInteractions(documentRepository, fileStorageService, modelMapper);
    }

    @Test
    void uploadDocument_approvedApplication_throwsBusinessException() throws IOException {

        // Arrange
        PassApplication application = application(ApplicationStatus.APPROVED);

        MultipartFile file = mockFile();

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> documentService.uploadDocument(APPLICATION_ID, file, DocumentType.STUDENT_ID, USER_ID));

        // Assert
        assertEquals("Documents can only be uploaded for pending or rejected applications", exception.getMessage());

        verifyNoInteractions(documentRepository, fileStorageService, modelMapper);
    }

    @Test
    void uploadDocument_nullDocumentType_throwsBusinessException() throws IOException {

        // Arrange
        PassApplication application = application(ApplicationStatus.PENDING);

        MultipartFile file = mockFile();

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> documentService.uploadDocument(APPLICATION_ID, file, null, USER_ID));

        // Assert
        assertEquals("Document type is required", exception.getMessage());

        verifyNoInteractions(documentRepository, fileStorageService, modelMapper);
    }

    @Test
    void uploadDocument_nullFile_throwsResourceNotFound() throws IOException {

        // Arrange
        PassApplication application = application(ApplicationStatus.PENDING);

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> documentService.uploadDocument(APPLICATION_ID, null, DocumentType.STUDENT_ID, USER_ID));

        // Assert
        assertEquals("File is required", exception.getMessage());

        verifyNoInteractions(documentRepository, fileStorageService, modelMapper);
    }

    @Test
    void uploadDocument_emptyFile_throwsResourceNotFound() throws IOException {

        // Arrange
        PassApplication application = application(ApplicationStatus.PENDING);

        MultipartFile file = mockFile();

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        when(file.isEmpty()).thenReturn(true);

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> documentService.uploadDocument(APPLICATION_ID, file, DocumentType.STUDENT_ID, USER_ID));

        // Assert
        assertEquals("File is required", exception.getMessage());

        verifyNoInteractions(documentRepository, fileStorageService, modelMapper);
    }

    @Test
    void uploadDocument_fileLargerThanFiveMb_throwsBusinessException() throws IOException {

        // Arrange
        PassApplication application = application(ApplicationStatus.PENDING);

        MultipartFile file = mockFile();

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        when(file.getSize()).thenReturn(5L * 1024 * 1024 + 1);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> documentService.uploadDocument(APPLICATION_ID, file, DocumentType.STUDENT_ID, USER_ID));

        // Assert
        assertEquals("File size must not exceed 5 MB.", exception.getMessage());

        verifyNoInteractions(documentRepository, fileStorageService, modelMapper);
    }

    @Test
    void uploadDocument_nullFilename_throwsBusinessException() throws IOException {

        // Arrange
        PassApplication application = application(ApplicationStatus.PENDING);

        MultipartFile file = mockFile();

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        when(file.getOriginalFilename()).thenReturn(null);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> documentService.uploadDocument(APPLICATION_ID, file, DocumentType.STUDENT_ID, USER_ID));

        // Assert
        assertEquals("File name is required", exception.getMessage());

        verifyNoInteractions(documentRepository, fileStorageService, modelMapper);
    }

    @Test
    void uploadDocument_blankFilename_throwsBusinessException() throws IOException {

        // Arrange
        PassApplication application = application(ApplicationStatus.PENDING);

        MultipartFile file = mockFile();

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        when(file.getOriginalFilename()).thenReturn("   ");

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> documentService.uploadDocument(APPLICATION_ID, file, DocumentType.STUDENT_ID, USER_ID));

        // Assert
        assertEquals("File name is required", exception.getMessage());

        verifyNoInteractions(documentRepository, fileStorageService, modelMapper);
    }

    @Test
    void uploadDocument_unsupportedExtension_throwsBusinessException() throws IOException {

        // Arrange
        PassApplication application = application(ApplicationStatus.PENDING);

        MultipartFile file = mockFile();

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        when(file.getOriginalFilename()).thenReturn("student-id.exe");

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> documentService.uploadDocument(APPLICATION_ID, file, DocumentType.STUDENT_ID, USER_ID));

        // Assert
        assertEquals("Only PDF, PNG, JPG, and JPEG files are allowed.", exception.getMessage());

        verifyNoInteractions(documentRepository, fileStorageService, modelMapper);
    }

    @Test
    void uploadDocument_pendingApplication_firstUpload_createsDocument() throws IOException {

        // Arrange
        PassApplication application = application(ApplicationStatus.PENDING);

        MultipartFile file = validFile("student-id.pdf", "application/pdf");

        UploadDocumentResponseDto expectedResponse = new UploadDocumentResponseDto();

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        when(documentRepository.findByPassApplicationApplicationIdAndDocumentType(APPLICATION_ID, DocumentType.STUDENT_ID)).thenReturn(Optional.empty());

        when(fileStorageService.uploadFile(file, APPLICATION_ID)).thenReturn(NEW_STORAGE_KEY);

        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document document = invocation.getArgument(0);
            document.setDocumentId(DOCUMENT_ID);
            return document;
        });

        when(modelMapper.map(any(Document.class), org.mockito.ArgumentMatchers.eq(UploadDocumentResponseDto.class))).thenReturn(expectedResponse);

        when(fileStorageService.getPresignedUrl(NEW_STORAGE_KEY)).thenReturn(PRESIGNED_URL);

        // Act
        UploadDocumentResponseDto result = documentService.uploadDocument(APPLICATION_ID, file, DocumentType.STUDENT_ID, USER_ID);

        // Assert
        assertSame(expectedResponse, result);

        assertEquals(PRESIGNED_URL, result.getFileUrl());

        assertEquals("Document uploaded successfully", result.getMessage());

        verify(documentRepository).save(org.mockito.ArgumentMatchers.argThat(document -> document.getPassApplication() == application && document.getDocumentType() == DocumentType.STUDENT_ID && "student-id.pdf".equals(document.getFileName()) && NEW_STORAGE_KEY.equals(document.getStorageKey()) && "application/pdf".equals(document.getContentType())));

        verify(fileStorageService, never()).deleteFile(any(String.class));
    }

    @Test
    void uploadDocument_rejectedApplication_firstUpload_isAllowed() throws IOException {

        // Arrange
        PassApplication application = application(ApplicationStatus.REJECTED);

        MultipartFile file = validFile("proof.png", "image/png");

        UploadDocumentResponseDto expectedResponse = new UploadDocumentResponseDto();

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        when(documentRepository.findByPassApplicationApplicationIdAndDocumentType(APPLICATION_ID, DocumentType.AGE_PROOF)).thenReturn(Optional.empty());

        when(fileStorageService.uploadFile(file, APPLICATION_ID)).thenReturn(NEW_STORAGE_KEY);

        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(modelMapper.map(any(Document.class), org.mockito.ArgumentMatchers.eq(UploadDocumentResponseDto.class))).thenReturn(expectedResponse);

        when(fileStorageService.getPresignedUrl(NEW_STORAGE_KEY)).thenReturn(PRESIGNED_URL);

        // Act
        UploadDocumentResponseDto result = documentService.uploadDocument(APPLICATION_ID, file, DocumentType.AGE_PROOF, USER_ID);

        // Assert
        assertSame(expectedResponse, result);

        assertEquals("Document uploaded successfully", result.getMessage());

        verify(documentRepository).save(any(Document.class));
    }

    @Test
    void uploadDocument_existingDocument_replacesMetadataAndDeletesOldFile() throws IOException {

        // Arrange
        PassApplication application = application(ApplicationStatus.REJECTED);

        MultipartFile file = validFile("updated-id.jpg", "image/jpeg");

        Document existingDocument = existingDocument(application, OLD_STORAGE_KEY);

        UploadDocumentResponseDto expectedResponse = new UploadDocumentResponseDto();

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        when(documentRepository.findByPassApplicationApplicationIdAndDocumentType(APPLICATION_ID, DocumentType.STUDENT_ID)).thenReturn(Optional.of(existingDocument));

        when(fileStorageService.uploadFile(file, APPLICATION_ID)).thenReturn(NEW_STORAGE_KEY);

        when(documentRepository.save(existingDocument)).thenReturn(existingDocument);

        when(modelMapper.map(existingDocument, UploadDocumentResponseDto.class)).thenReturn(expectedResponse);

        when(fileStorageService.getPresignedUrl(NEW_STORAGE_KEY)).thenReturn(PRESIGNED_URL);

        // Act
        UploadDocumentResponseDto result = documentService.uploadDocument(APPLICATION_ID, file, DocumentType.STUDENT_ID, USER_ID);

        // Assert
        assertSame(expectedResponse, result);

        assertEquals("updated-id.jpg", existingDocument.getFileName());

        assertEquals(NEW_STORAGE_KEY, existingDocument.getStorageKey());

        assertEquals("image/jpeg", existingDocument.getContentType());

        assertEquals(PRESIGNED_URL, result.getFileUrl());

        assertEquals("Document replaced successfully", result.getMessage());

        verify(documentRepository).save(existingDocument);

        verify(fileStorageService).deleteFile(OLD_STORAGE_KEY);
    }

    @Test
    void uploadDocument_replacementUsesSameStorageKey_doesNotDeleteFile() throws IOException {

        // Arrange
        PassApplication application = application(ApplicationStatus.PENDING);

        MultipartFile file = validFile("updated.pdf", "application/pdf");

        Document existingDocument = existingDocument(application, OLD_STORAGE_KEY);

        UploadDocumentResponseDto expectedResponse = new UploadDocumentResponseDto();

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        when(documentRepository.findByPassApplicationApplicationIdAndDocumentType(APPLICATION_ID, DocumentType.STUDENT_ID)).thenReturn(Optional.of(existingDocument));

        when(fileStorageService.uploadFile(file, APPLICATION_ID)).thenReturn(OLD_STORAGE_KEY);

        when(documentRepository.save(existingDocument)).thenReturn(existingDocument);

        when(modelMapper.map(existingDocument, UploadDocumentResponseDto.class)).thenReturn(expectedResponse);

        when(fileStorageService.getPresignedUrl(OLD_STORAGE_KEY)).thenReturn(PRESIGNED_URL);

        // Act
        UploadDocumentResponseDto result = documentService.uploadDocument(APPLICATION_ID, file, DocumentType.STUDENT_ID, USER_ID);

        // Assert
        assertSame(expectedResponse, result);

        assertEquals("Document replaced successfully", result.getMessage());

        verify(fileStorageService, never()).deleteFile(any(String.class));
    }

    @Test
    void uploadDocument_databaseSaveFails_deletesNewlyUploadedFile() throws IOException {

        // Arrange
        PassApplication application = application(ApplicationStatus.PENDING);

        MultipartFile file = validFile("student-id.pdf", "application/pdf");

        RuntimeException databaseException = new RuntimeException("Database failure");

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        when(documentRepository.findByPassApplicationApplicationIdAndDocumentType(APPLICATION_ID, DocumentType.STUDENT_ID)).thenReturn(Optional.empty());

        when(fileStorageService.uploadFile(file, APPLICATION_ID)).thenReturn(NEW_STORAGE_KEY);

        when(documentRepository.save(any(Document.class))).thenThrow(databaseException);

        // Act
        RuntimeException exception = assertThrows(RuntimeException.class, () -> documentService.uploadDocument(APPLICATION_ID, file, DocumentType.STUDENT_ID, USER_ID));

        // Assert
        assertSame(databaseException, exception);

        verify(fileStorageService).deleteFile(NEW_STORAGE_KEY);

        verifyNoInteractions(modelMapper);
    }

    @Test
    void uploadDocument_replacementDatabaseSaveFails_deletesNewFileButKeepsOldFile() throws IOException {

        // Arrange
        PassApplication application = application(ApplicationStatus.REJECTED);

        MultipartFile file = validFile("updated.pdf", "application/pdf");

        Document existingDocument = existingDocument(application, OLD_STORAGE_KEY);

        RuntimeException databaseException = new RuntimeException("Database failure");

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        when(documentRepository.findByPassApplicationApplicationIdAndDocumentType(APPLICATION_ID, DocumentType.STUDENT_ID)).thenReturn(Optional.of(existingDocument));

        when(fileStorageService.uploadFile(file, APPLICATION_ID)).thenReturn(NEW_STORAGE_KEY);

        when(documentRepository.save(existingDocument)).thenThrow(databaseException);

        // Act
        RuntimeException exception = assertThrows(RuntimeException.class, () -> documentService.uploadDocument(APPLICATION_ID, file, DocumentType.STUDENT_ID, USER_ID));

        // Assert
        assertSame(databaseException, exception);

        verify(fileStorageService).deleteFile(NEW_STORAGE_KEY);

        verify(fileStorageService, never()).deleteFile(OLD_STORAGE_KEY);

        verifyNoInteractions(modelMapper);
    }

    // -------------------------------------------------------------------------
    // getDocumentsByApplication
    // -------------------------------------------------------------------------

    @Test
    void getDocumentsByApplication_userNotFound_throwsResourceNotFound() {

        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> documentService.getDocumentsByApplication(APPLICATION_ID, USER_ID));

        // Assert
        assertEquals("User not found", exception.getMessage());

        verifyNoInteractions(passApplicationRepository, documentRepository, modelMapper, fileStorageService);
    }

    @Test
    void getDocumentsByApplication_passengerDoesNotOwnApplication_throwsAccessDenied() {

        // Arrange
        Users passenger = userWithRole(Role.PASSENGER);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(passenger));

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(null);

        // Act
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> documentService.getDocumentsByApplication(APPLICATION_ID, USER_ID));

        // Assert
        assertEquals("You are not authorized to view documents for this application", exception.getMessage());

        verifyNoInteractions(documentRepository, modelMapper, fileStorageService);
    }

    @Test
    void getDocumentsByApplication_passengerOwnsApplication_returnsDocuments() {

        // Arrange
        Users passenger = userWithRole(Role.PASSENGER);

        PassApplication application = application(ApplicationStatus.PENDING);

        Document document = existingDocument(application, NEW_STORAGE_KEY);

        DocumentResponseDto mappedResponse = DocumentResponseDto.builder().documentId(DOCUMENT_ID).documentType(DocumentType.STUDENT_ID).fileName("student-id.pdf").build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(passenger));

        when(passApplicationRepository.findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID)).thenReturn(application);

        when(documentRepository.findAllByPassApplicationApplicationId(APPLICATION_ID)).thenReturn(List.of(document));

        when(modelMapper.map(document, DocumentResponseDto.class)).thenReturn(mappedResponse);

        when(fileStorageService.getPresignedUrl(NEW_STORAGE_KEY)).thenReturn(PRESIGNED_URL);

        // Act
        List<DocumentResponseDto> result = documentService.getDocumentsByApplication(APPLICATION_ID, USER_ID);

        // Assert
        assertEquals(1, result.size());

        assertSame(mappedResponse, result.get(0));

        assertEquals(PRESIGNED_URL, result.get(0).getFileUrl());

        verify(documentRepository).findAllByPassApplicationApplicationId(APPLICATION_ID);
    }

    @Test
    void getDocumentsByApplication_officerCanViewAnyApplicationDocuments() {

        // Arrange
        Users officer = userWithRole(Role.PASS_OFFICER);

        PassApplication application = application(ApplicationStatus.APPROVED);

        Document document = existingDocument(application, NEW_STORAGE_KEY);

        DocumentResponseDto mappedResponse = new DocumentResponseDto();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(officer));

        when(documentRepository.findAllByPassApplicationApplicationId(APPLICATION_ID)).thenReturn(List.of(document));

        when(modelMapper.map(document, DocumentResponseDto.class)).thenReturn(mappedResponse);

        when(fileStorageService.getPresignedUrl(NEW_STORAGE_KEY)).thenReturn(PRESIGNED_URL);

        // Act
        List<DocumentResponseDto> result = documentService.getDocumentsByApplication(APPLICATION_ID, USER_ID);

        // Assert
        assertEquals(1, result.size());

        assertEquals(PRESIGNED_URL, result.get(0).getFileUrl());

        verify(passApplicationRepository, never()).findByApplicationIdAndPassengerUserId(APPLICATION_ID, USER_ID);
    }

    @Test
    void getDocumentsByApplication_noDocuments_returnsEmptyList() {

        // Arrange
        Users officer = userWithRole(Role.PASS_OFFICER);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(officer));

        when(documentRepository.findAllByPassApplicationApplicationId(APPLICATION_ID)).thenReturn(List.of());

        // Act
        List<DocumentResponseDto> result = documentService.getDocumentsByApplication(APPLICATION_ID, USER_ID);

        // Assert
        assertTrue(result.isEmpty());

        verifyNoInteractions(modelMapper, fileStorageService);
    }

    @Test
    void getDocumentsByApplication_admin_isDenied() {

        // Arrange
        Users admin = userWithRole(Role.ADMIN);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(admin));

        // Act
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> documentService.getDocumentsByApplication(APPLICATION_ID, USER_ID));

        // Assert
        assertEquals("You are not authorized to view this documents", exception.getMessage());

        verifyNoInteractions(passApplicationRepository, documentRepository, modelMapper, fileStorageService);
    }

    @Test
    void getDocumentsByApplication_ticketChecker_isDenied() {

        // Arrange
        Users ticketChecker = userWithRole(Role.TICKET_CHECKER);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(ticketChecker));

        // Act
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> documentService.getDocumentsByApplication(APPLICATION_ID, USER_ID));

        // Assert
        assertEquals("You are not authorized to view this documents", exception.getMessage());

        verifyNoInteractions(passApplicationRepository, documentRepository, modelMapper, fileStorageService);
    }

    @Test
    void getDocumentsByApplication_nullRole_isDenied() {

        // Arrange
        Users user = userWithRole(null);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        // Act
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> documentService.getDocumentsByApplication(APPLICATION_ID, USER_ID));

        // Assert
        assertEquals("You are not authorized to view this documents", exception.getMessage());

        verifyNoInteractions(passApplicationRepository, documentRepository, modelMapper, fileStorageService);
    }


    // -------------------------------------------------------------------------
    // deleteDocument
    // -------------------------------------------------------------------------

    @Test
    void deleteDocument_nullDocumentId_throwsResourceNotFound() {

        // Arrange

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> documentService.deleteDocument(null, USER_ID));

        // Assert
        assertEquals("Document id not found while deleting document", exception.getMessage());

        verifyNoInteractions(documentRepository, fileStorageService);
    }

    @Test
    void deleteDocument_documentNotOwnedByPassenger_throwsAccessDenied() {

        // Arrange
        when(documentRepository.findByDocumentIdAndPassApplicationPassengerUserId(DOCUMENT_ID, USER_ID)).thenReturn(Optional.empty());

        // Act
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> documentService.deleteDocument(DOCUMENT_ID, USER_ID));

        // Assert
        assertEquals("You are not authorized to delete this document", exception.getMessage());

        verify(documentRepository, never()).deleteById(DOCUMENT_ID);

        verifyNoInteractions(fileStorageService);
    }

    @Test
    void deleteDocument_approvedApplication_throwsBusinessException() {

        // Arrange
        PassApplication application = application(ApplicationStatus.APPROVED);

        Document document = existingDocument(application, OLD_STORAGE_KEY);

        when(documentRepository.findByDocumentIdAndPassApplicationPassengerUserId(DOCUMENT_ID, USER_ID)).thenReturn(Optional.of(document));

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> documentService.deleteDocument(DOCUMENT_ID, USER_ID));

        // Assert
        assertEquals("Document cannot be deleted for approved applications", exception.getMessage());

        verify(documentRepository, never()).deleteById(DOCUMENT_ID);

        verifyNoInteractions(fileStorageService);
    }

    @Test
    void deleteDocument_pendingApplication_deletesDatabaseAndStoredFile() {

        // Arrange
        PassApplication application = application(ApplicationStatus.PENDING);

        Document document = existingDocument(application, OLD_STORAGE_KEY);

        when(documentRepository.findByDocumentIdAndPassApplicationPassengerUserId(DOCUMENT_ID, USER_ID)).thenReturn(Optional.of(document));

        // Act
        documentService.deleteDocument(DOCUMENT_ID, USER_ID);

        // Assert
        verify(documentRepository).deleteById(DOCUMENT_ID);

        verify(fileStorageService).deleteFile(OLD_STORAGE_KEY);
    }

    @Test
    void deleteDocument_rejectedApplication_deletesDatabaseAndStoredFile() {

        // Arrange
        PassApplication application = application(ApplicationStatus.REJECTED);

        Document document = existingDocument(application, OLD_STORAGE_KEY);

        when(documentRepository.findByDocumentIdAndPassApplicationPassengerUserId(DOCUMENT_ID, USER_ID)).thenReturn(Optional.of(document));

        // Act
        documentService.deleteDocument(DOCUMENT_ID, USER_ID);

        // Assert
        verify(documentRepository).deleteById(DOCUMENT_ID);

        verify(fileStorageService).deleteFile(OLD_STORAGE_KEY);
    }


    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private MultipartFile mockFile() {

        return org.mockito.Mockito.mock(MultipartFile.class);
    }

    private MultipartFile validFile(String filename, String contentType) {

        MultipartFile file = mockFile();

        when(file.isEmpty()).thenReturn(false);

        when(file.getSize()).thenReturn(1024L);

        when(file.getOriginalFilename()).thenReturn(filename);

        when(file.getContentType()).thenReturn(contentType);

        return file;
    }

    private Users userWithRole(Role role) {

        return Users.builder().userId(USER_ID).username("passenger").role(role).build();
    }

    private PassApplication application(ApplicationStatus status) {

        return PassApplication.builder().applicationId(APPLICATION_ID).applicationNumber("APP-12345678").passenger(userWithRole(Role.PASSENGER)).applicationStatus(status).build();
    }

    private Document existingDocument(PassApplication application, String storageKey) {

        return Document.builder().documentId(DOCUMENT_ID).passApplication(application).documentType(DocumentType.STUDENT_ID).fileName("student-id.pdf").storageKey(storageKey).contentType("application/pdf").build();
    }
}