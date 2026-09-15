package com.porfolio.EPassSystemSpringboot.repositories;

import com.porfolio.EPassSystemSpringboot.entities.Document;
import com.porfolio.EPassSystemSpringboot.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findAllByPassApplicationApplicationId(Long applicationId);

    Optional<Document> findByDocumentIdAndPassApplicationPassengerUserId(Long documentId, Long userId);

    //ownership check query
    List<Document> findAllByPassApplicationApplicationIdAndPassApplicationPassengerUserId(Long applicationId, Long userId);

    //Application 101 + STUDENT_ID   ->    existing Document?
    Optional<Document> findByPassApplicationApplicationIdAndDocumentType(Long applicationId, DocumentType documentType);
}
