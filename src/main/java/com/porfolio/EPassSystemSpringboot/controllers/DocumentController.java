package com.porfolio.EPassSystemSpringboot.controllers;

import com.porfolio.EPassSystemSpringboot.dtos.DocumentResponseDto;
import com.porfolio.EPassSystemSpringboot.dtos.UploadDocumentResponseDto;
import com.porfolio.EPassSystemSpringboot.entities.Users;
import com.porfolio.EPassSystemSpringboot.enums.DocumentType;
import com.porfolio.EPassSystemSpringboot.services.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<UploadDocumentResponseDto> uploadDocument(
            @RequestParam("applicationId") Long applicationId,
            @RequestParam("documentType") DocumentType documentType,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal Users user) throws IOException {

        UploadDocumentResponseDto uploadDocumentResponseDto = documentService.uploadDocument(applicationId, file, documentType, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadDocumentResponseDto);
    }

    @GetMapping("/{applicationId}")
    @PreAuthorize("hasAnyRole('PASS_OFFICER', 'PASSENGER')")
    public ResponseEntity<List<DocumentResponseDto>> getAllDocumentsByApplicationId(
            @AuthenticationPrincipal Users user,
            @PathVariable Long applicationId) {

        List<DocumentResponseDto> documentsByApplication = documentService.getDocumentsByApplication(applicationId, user.getUserId());
        return ResponseEntity.ok(documentsByApplication);
    }


    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Void> deleteDocument(
            @AuthenticationPrincipal Users user,
            @PathVariable Long documentId
    ) {

        documentService.deleteDocument(documentId, user.getUserId());
        return ResponseEntity.noContent().build();
    }


}
