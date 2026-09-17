package com.porfolio.EPassSystemSpringboot.entities;

import com.porfolio.EPassSystemSpringboot.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "documents",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_application_document_type", columnNames = {"application_id", "document_type"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private PassApplication passApplication;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String storageKey;

    @Column(nullable = false)
    private String contentType;

}
