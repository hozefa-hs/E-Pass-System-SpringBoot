package com.porfolio.EPassSystemSpringboot.repositories;

import com.porfolio.EPassSystemSpringboot.entities.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findAllByPassApplicationApplicationId(Long applicationId);

    Optional<Document> findByDocumentIdAndPassApplicationPassengerUserId(Long documentId, Long userId);
}
