package com.porfolio.EPassSystemSpringboot.services;

import com.porfolio.EPassSystemSpringboot.dtos.PassApplicationResponseDto;
import com.porfolio.EPassSystemSpringboot.entities.Document;
import com.porfolio.EPassSystemSpringboot.entities.PassApplication;
import com.porfolio.EPassSystemSpringboot.enums.ApplicationStatus;
import com.porfolio.EPassSystemSpringboot.enums.DocumentType;
import com.porfolio.EPassSystemSpringboot.enums.PassType;
import com.porfolio.EPassSystemSpringboot.enums.PassValidity;
import com.porfolio.EPassSystemSpringboot.exceptions.BusinessException;
import com.porfolio.EPassSystemSpringboot.exceptions.ResourceNotFoundException;
import com.porfolio.EPassSystemSpringboot.repositories.DocumentRepository;
import com.porfolio.EPassSystemSpringboot.repositories.PassApplicationRepository;
import com.porfolio.EPassSystemSpringboot.services.implementations.ReviewServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    private static final Long APPLICATION_ID = 10L;
    private static final Long PASS_OFFICER_ID = 20L;

    @Mock
    private PassApplicationRepository passApplicationRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;


    // -------------------------------------------------------------------------
    // approveApplication
    // -------------------------------------------------------------------------

    @Test
    void approveApplication_applicationNotFound_throwsResourceNotFound() {

        // Arrange
        when(passApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> reviewService.approveApplication(APPLICATION_ID, PASS_OFFICER_ID));

        // Assert
        assertEquals("Pass application with id 10 not found", exception.getMessage());

        verifyNoInteractions(documentRepository, modelMapper);

        verify(passApplicationRepository, never()).save(org.mockito.ArgumentMatchers.any(PassApplication.class));
    }

    @Test
    void approveApplication_nonPendingApplication_throwsBusinessException() {

        // Arrange
        PassApplication application = application(PassType.STUDENT, ApplicationStatus.APPROVED);

        when(passApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> reviewService.approveApplication(APPLICATION_ID, PASS_OFFICER_ID));

        // Assert
        assertEquals("Only pending applications can be approved", exception.getMessage());

        verifyNoInteractions(documentRepository, modelMapper);

        verify(passApplicationRepository, never()).save(application);
    }

    @Test
    void approveApplication_requiredDocumentMissing_throwsBusinessException() {

        // Arrange
        PassApplication application = application(PassType.STUDENT, ApplicationStatus.PENDING);

        when(passApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        when(documentRepository.findByPassApplicationApplicationIdAndDocumentType(APPLICATION_ID, DocumentType.STUDENT_ID)).thenReturn(Optional.empty());

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> reviewService.approveApplication(APPLICATION_ID, PASS_OFFICER_ID));

        // Assert
        assertEquals("Required document is missing: STUDENT_ID", exception.getMessage());

        verify(documentRepository).findByPassApplicationApplicationIdAndDocumentType(APPLICATION_ID, DocumentType.STUDENT_ID);

        verify(passApplicationRepository, never()).save(application);

        verifyNoInteractions(modelMapper);
    }

    @Test
    void approveApplication_studentWithStudentId_isApproved() {

        // Arrange
        PassApplication application = application(PassType.STUDENT, ApplicationStatus.PENDING);

        application.setRejectionReason("Previous rejection");

        Document document = document(DocumentType.STUDENT_ID);

        PassApplicationResponseDto expectedResponse = responseDto(ApplicationStatus.APPROVED);

        when(passApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        when(documentRepository.findByPassApplicationApplicationIdAndDocumentType(APPLICATION_ID, DocumentType.STUDENT_ID)).thenReturn(Optional.of(document));

        when(passApplicationRepository.save(application)).thenReturn(application);

        when(modelMapper.map(application, PassApplicationResponseDto.class)).thenReturn(expectedResponse);

        // Act
        PassApplicationResponseDto result = reviewService.approveApplication(APPLICATION_ID, PASS_OFFICER_ID);

        // Assert
        assertSame(expectedResponse, result);

        assertEquals(ApplicationStatus.APPROVED, application.getApplicationStatus());

        assertNull(application.getRejectionReason());

        assertEquals(PASS_OFFICER_ID, application.getReviewedByOfficerId());

        assertNotNull(application.getReviewedAt());

        verify(documentRepository).findByPassApplicationApplicationIdAndDocumentType(APPLICATION_ID, DocumentType.STUDENT_ID);

        verify(passApplicationRepository).save(application);

        verify(modelMapper).map(application, PassApplicationResponseDto.class);
    }

    @Test
    void approveApplication_seniorCitizenWithAgeProof_isApproved() {

        // Arrange
        PassApplication application = application(PassType.SENIOR_CITIZEN, ApplicationStatus.PENDING);

        Document document = document(DocumentType.AGE_PROOF);

        PassApplicationResponseDto expectedResponse = responseDto(ApplicationStatus.APPROVED);

        when(passApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        when(documentRepository.findByPassApplicationApplicationIdAndDocumentType(APPLICATION_ID, DocumentType.AGE_PROOF)).thenReturn(Optional.of(document));

        when(passApplicationRepository.save(application)).thenReturn(application);

        when(modelMapper.map(application, PassApplicationResponseDto.class)).thenReturn(expectedResponse);

        // Act
        PassApplicationResponseDto result = reviewService.approveApplication(APPLICATION_ID, PASS_OFFICER_ID);

        // Assert
        assertSame(expectedResponse, result);

        assertEquals(ApplicationStatus.APPROVED, application.getApplicationStatus());

        assertEquals(PASS_OFFICER_ID, application.getReviewedByOfficerId());

        assertNotNull(application.getReviewedAt());

        verify(documentRepository).findByPassApplicationApplicationIdAndDocumentType(APPLICATION_ID, DocumentType.AGE_PROOF);

        verify(passApplicationRepository).save(application);
    }

    @Test
    void approveApplication_corporateWithCorporateId_isApproved() {

        // Arrange
        PassApplication application = application(PassType.CORPORATE, ApplicationStatus.PENDING);

        Document document = document(DocumentType.CORPORATE_ID);

        PassApplicationResponseDto expectedResponse = responseDto(ApplicationStatus.APPROVED);

        when(passApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        when(documentRepository.findByPassApplicationApplicationIdAndDocumentType(APPLICATION_ID, DocumentType.CORPORATE_ID)).thenReturn(Optional.of(document));

        when(passApplicationRepository.save(application)).thenReturn(application);

        when(modelMapper.map(application, PassApplicationResponseDto.class)).thenReturn(expectedResponse);

        // Act
        PassApplicationResponseDto result = reviewService.approveApplication(APPLICATION_ID, PASS_OFFICER_ID);

        // Assert
        assertSame(expectedResponse, result);

        assertEquals(ApplicationStatus.APPROVED, application.getApplicationStatus());

        assertEquals(PASS_OFFICER_ID, application.getReviewedByOfficerId());

        assertNotNull(application.getReviewedAt());

        verify(documentRepository).findByPassApplicationApplicationIdAndDocumentType(APPLICATION_ID, DocumentType.CORPORATE_ID);

        verify(passApplicationRepository).save(application);
    }

    @Test
    void approveApplication_nullPassType_throwsNullPointerException() {

        // Arrange
        PassApplication application = application(null, ApplicationStatus.PENDING);

        when(passApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        // Act
        assertThrows(NullPointerException.class, () -> reviewService.approveApplication(APPLICATION_ID, PASS_OFFICER_ID));

        // Assert
        verifyNoInteractions(documentRepository, modelMapper);

        verify(passApplicationRepository, never()).save(application);
    }


    // -------------------------------------------------------------------------
    // rejectApplication
    // -------------------------------------------------------------------------

    @Test
    void rejectApplication_applicationNotFound_throwsResourceNotFound() {

        // Arrange
        when(passApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> reviewService.rejectApplication(APPLICATION_ID, PASS_OFFICER_ID, "Invalid document"));

        // Assert
        assertEquals("Pass application with id 10 not found", exception.getMessage());

        verifyNoInteractions(modelMapper);

        verify(passApplicationRepository, never()).save(org.mockito.ArgumentMatchers.any(PassApplication.class));
    }

    @Test
    void rejectApplication_nonPendingApplication_throwsBusinessException() {

        // Arrange
        PassApplication application = application(PassType.STUDENT, ApplicationStatus.APPROVED);

        when(passApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> reviewService.rejectApplication(APPLICATION_ID, PASS_OFFICER_ID, "Invalid document"));

        // Assert
        assertEquals("Only pending applications can be rejected", exception.getMessage());

        verify(passApplicationRepository, never()).save(application);

        verifyNoInteractions(modelMapper);
    }

    @Test
    void rejectApplication_pendingApplication_setsRejectedState() {

        // Arrange
        String rejectionReason = "Student ID is not readable";

        PassApplication application = application(PassType.STUDENT, ApplicationStatus.PENDING);

        PassApplicationResponseDto expectedResponse = responseDto(ApplicationStatus.REJECTED);

        when(passApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        when(passApplicationRepository.save(application)).thenReturn(application);

        when(modelMapper.map(application, PassApplicationResponseDto.class)).thenReturn(expectedResponse);

        // Act
        PassApplicationResponseDto result = reviewService.rejectApplication(APPLICATION_ID, PASS_OFFICER_ID, rejectionReason);

        // Assert
        assertSame(expectedResponse, result);

        assertEquals(ApplicationStatus.REJECTED, application.getApplicationStatus());

        assertEquals(rejectionReason, application.getRejectionReason());

        assertEquals(PASS_OFFICER_ID, application.getReviewedByOfficerId());

        assertNotNull(application.getReviewedAt());

        verify(passApplicationRepository).save(application);

        verify(modelMapper).map(application, PassApplicationResponseDto.class);

        verifyNoInteractions(documentRepository);
    }

    @Test
    void rejectApplication_nullReason_currentServiceAllowsNull() {

        // Arrange
        PassApplication application = application(PassType.STUDENT, ApplicationStatus.PENDING);

        PassApplicationResponseDto expectedResponse = responseDto(ApplicationStatus.REJECTED);

        when(passApplicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));

        when(passApplicationRepository.save(application)).thenReturn(application);

        when(modelMapper.map(application, PassApplicationResponseDto.class)).thenReturn(expectedResponse);

        // Act
        PassApplicationResponseDto result = reviewService.rejectApplication(APPLICATION_ID, PASS_OFFICER_ID, null);

        // Assert
        assertSame(expectedResponse, result);

        assertEquals(ApplicationStatus.REJECTED, application.getApplicationStatus());

        assertNull(application.getRejectionReason());

        assertEquals(PASS_OFFICER_ID, application.getReviewedByOfficerId());

        assertNotNull(application.getReviewedAt());

        verify(passApplicationRepository).save(application);

        verifyNoInteractions(documentRepository);
    }


    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private PassApplication application(PassType passType, ApplicationStatus status) {

        return PassApplication.builder().applicationId(APPLICATION_ID).applicationNumber("APP-12345678").passType(passType).passValidity(PassValidity.ONE_MONTH).applicationStatus(status).build();
    }

    private Document document(DocumentType documentType) {

        return Document.builder().documentId(100L).documentType(documentType).fileName("document.pdf").storageKey("documents/10/document.pdf").contentType("application/pdf").build();
    }

    private PassApplicationResponseDto responseDto(ApplicationStatus status) {

        PassApplicationResponseDto response = new PassApplicationResponseDto();

        response.setApplicationId(APPLICATION_ID);
        response.setApplicationNumber("APP-12345678");
        response.setPassType(PassType.STUDENT);
        response.setPassValidity(PassValidity.ONE_MONTH);
        response.setApplicationStatus(status);

        return response;
    }
}