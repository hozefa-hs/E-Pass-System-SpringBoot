package com.porfolio.EPassSystemSpringboot.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private Instant timeStamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> validationErrors;

}
