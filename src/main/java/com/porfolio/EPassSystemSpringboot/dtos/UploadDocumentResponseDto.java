package com.porfolio.EPassSystemSpringboot.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadDocumentResponseDto {

    private Long documentId;

    private String fileName;

    private String fileUrl;

    private String message;
}
