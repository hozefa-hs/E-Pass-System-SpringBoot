package com.porfolio.EPassSystemSpringboot.dtos;

import com.porfolio.EPassSystemSpringboot.enums.DocumentType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentResponseDto {

    private Long documentId;

    private DocumentType documentType;

    private String fileName;

    private String fileUrl;

    private String contentType;
}
