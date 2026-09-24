package com.porfolio.EPassSystemSpringboot.services.implementations;

import com.porfolio.EPassSystemSpringboot.dtos.PassApplicationResponseDto;
import com.porfolio.EPassSystemSpringboot.entities.PassApplication;
import com.porfolio.EPassSystemSpringboot.enums.ApplicationStatus;
import com.porfolio.EPassSystemSpringboot.enums.DocumentType;
import com.porfolio.EPassSystemSpringboot.enums.PassType;
import com.porfolio.EPassSystemSpringboot.exceptions.BusinessException;
import com.porfolio.EPassSystemSpringboot.exceptions.ResourceNotFoundException;
import com.porfolio.EPassSystemSpringboot.repositories.DocumentRepository;
import com.porfolio.EPassSystemSpringboot.repositories.PassApplicationRepository;
import com.porfolio.EPassSystemSpringboot.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final PassApplicationRepository passApplicationRepository;
    private final DocumentRepository documentRepository;
    private final ModelMapper modelMapper;

    @Override
    public PassApplicationResponseDto approveApplication(Long applicationId, Long passOfficerId) {

        PassApplication passApplication = passApplicationRepository.findById(applicationId).orElseThrow(() -> new ResourceNotFoundException("Pass application with id " + applicationId + " not found"));

        if(passApplication.getApplicationStatus() != ApplicationStatus.PENDING) {
            throw new BusinessException("Only pending applications can be approved");
        }

        // Validate required document before approval
        DocumentType requiredDocumentType = getRequiredDocumentType(passApplication.getPassType());

        boolean documentExists  = documentRepository.findByPassApplicationApplicationIdAndDocumentType(applicationId, requiredDocumentType).isPresent();

        if(!documentExists){
            throw new BusinessException("Required document is missing: " + requiredDocumentType);
        }


        //approving application
        passApplication.setApplicationStatus(ApplicationStatus.APPROVED);
        passApplication.setRejectionReason(null);
        passApplication.setReviewedAt(LocalDateTime.now());
        passApplication.setReviewedByOfficerId(passOfficerId);

        PassApplication savedApplication = passApplicationRepository.save(passApplication);

        return modelMapper.map(savedApplication, PassApplicationResponseDto.class);
    }

    private DocumentType getRequiredDocumentType(PassType passType) {

        return switch (passType) {
            case STUDENT -> DocumentType.STUDENT_ID;
            case SENIOR_CITIZEN -> DocumentType.AGE_PROOF;
            case CORPORATE -> DocumentType.CORPORATE_ID;
        };
    }


    @Override
    public PassApplicationResponseDto rejectApplication(Long applicationId, Long passOfficerId, String rejectionReason) {

        PassApplication passApplication = passApplicationRepository.findById(applicationId).orElseThrow(() -> new ResourceNotFoundException("Pass application with id " + applicationId + " not found"));

        if(passApplication.getApplicationStatus() != ApplicationStatus.PENDING) {
            throw new BusinessException("Only pending applications can be rejected");
        }

        passApplication.setApplicationStatus(ApplicationStatus.REJECTED);
        passApplication.setRejectionReason(rejectionReason);
        passApplication.setReviewedAt(LocalDateTime.now());
        passApplication.setReviewedByOfficerId(passOfficerId);

        PassApplication savedApplication = passApplicationRepository.save(passApplication);

        return modelMapper.map(savedApplication, PassApplicationResponseDto.class);
    }
}
