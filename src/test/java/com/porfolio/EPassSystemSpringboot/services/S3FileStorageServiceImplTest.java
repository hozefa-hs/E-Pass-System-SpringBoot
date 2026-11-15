package com.porfolio.EPassSystemSpringboot.services;


import com.porfolio.EPassSystemSpringboot.exceptions.ResourceNotFoundException;
import com.porfolio.EPassSystemSpringboot.services.implementations.S3FileStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3FileStorageServiceImplTest {

    private static final String BUCKET_NAME = "e-pass-test-bucket";

    private static final Long APPLICATION_ID = 101L;

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private MultipartFile file;

    private S3FileStorageServiceImpl s3FileStorageService;


    @BeforeEach
    void setUp() {

        // Arrange
        s3FileStorageService = new S3FileStorageServiceImpl(s3Client, s3Presigner);

        ReflectionTestUtils.setField(s3FileStorageService, "bucketName", BUCKET_NAME);
    }


    // -------------------------------------------------------------------------
    // uploadFile
    // -------------------------------------------------------------------------

    @Test
    void uploadFile_validFile_uploadsToExpectedBucketAndReturnsGeneratedObjectKey() throws IOException {

        // Arrange
        byte[] fileContent = "sample-document-content".getBytes();

        when(file.getOriginalFilename()).thenReturn("student-id.pdf");

        when(file.getContentType()).thenReturn("application/pdf");

        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent));

        when(file.getSize()).thenReturn((long) fileContent.length);

        PutObjectResponse response = successfulPutObjectResponse();

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(response);

        // Act
        String result = s3FileStorageService.uploadFile(file, APPLICATION_ID);

        // Assert
        assertNotNull(result);

        assertTrue(result.startsWith("documents/101/"));

        assertTrue(result.endsWith("-student-id.pdf"));

        String generatedPart = result.substring("documents/101/".length(), result.length() - "-student-id.pdf".length());

        UUID.fromString(generatedPart);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);

        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

        PutObjectRequest capturedRequest = requestCaptor.getValue();

        assertEquals(BUCKET_NAME, capturedRequest.bucket());

        assertEquals(result, capturedRequest.key());

        assertEquals("application/pdf", capturedRequest.contentType());
    }

    @Test
    void uploadFile_validFile_readsInputStreamAndSize() throws IOException {

        // Arrange
        byte[] fileContent = "document".getBytes();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContent);

        when(file.getOriginalFilename()).thenReturn("proof.png");

        when(file.getContentType()).thenReturn("image/png");

        when(file.getInputStream()).thenReturn(inputStream);

        when(file.getSize()).thenReturn((long) fileContent.length);

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(successfulPutObjectResponse());

        // Act
        s3FileStorageService.uploadFile(file, APPLICATION_ID);

        // Assert
        verify(file).getInputStream();

        verify(file).getSize();

        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadFile_s3UploadFails_wrapsRuntimeException() throws IOException {

        // Arrange
        byte[] fileContent = "document".getBytes();

        when(file.getOriginalFilename()).thenReturn("proof.pdf");

        when(file.getContentType()).thenReturn("application/pdf");

        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent));

        when(file.getSize()).thenReturn((long) fileContent.length);

        RuntimeException s3Exception = new RuntimeException("S3 unavailable");

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenThrow(s3Exception);

        // Act
        RuntimeException exception = assertThrows(RuntimeException.class, () -> s3FileStorageService.uploadFile(file, APPLICATION_ID));

        // Assert
        assertSame(s3Exception, exception.getCause());

        assertEquals("java.lang.RuntimeException: S3 unavailable", exception.getMessage());
    }

    @Test
    void uploadFile_inputStreamFails_propagatesIOException() throws IOException {

        // Arrange
        when(file.getOriginalFilename()).thenReturn("proof.pdf");

        when(file.getContentType()).thenReturn("application/pdf");

        IOException ioException = new IOException("Unable to read upload");

        when(file.getInputStream()).thenThrow(ioException);

        // Act
        IOException exception = assertThrows(IOException.class, () -> s3FileStorageService.uploadFile(file, APPLICATION_ID));

        // Assert
        assertSame(ioException, exception);

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }


    // -------------------------------------------------------------------------
    // getPresignedUrl
    // -------------------------------------------------------------------------

    @Test
    void getPresignedUrl_validObjectKey_returnsPresignedUrl() throws MalformedURLException {

        // Arrange
        String objectKey = "documents/101/document.pdf";

        String expectedUrl = "https://example.com/document.pdf";

        PresignedGetObjectRequest presignedRequest = org.mockito.Mockito.mock(PresignedGetObjectRequest.class);

        when(presignedRequest.url()).thenReturn(new URL(expectedUrl));

        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedRequest);

        // Act
        String result = s3FileStorageService.getPresignedUrl(objectKey);

        // Assert
        assertEquals(expectedUrl, result);

        ArgumentCaptor<GetObjectPresignRequest> requestCaptor = ArgumentCaptor.forClass(GetObjectPresignRequest.class);

        verify(s3Presigner).presignGetObject(requestCaptor.capture());

        GetObjectPresignRequest capturedRequest = requestCaptor.getValue();

        assertEquals(Duration.ofHours(1), capturedRequest.signatureDuration());

        assertEquals(BUCKET_NAME, capturedRequest.getObjectRequest().bucket());

        assertEquals(objectKey, capturedRequest.getObjectRequest().key());

        verifyNoInteractions(s3Client);
    }


    // -------------------------------------------------------------------------
    // deleteFile
    // -------------------------------------------------------------------------

    @Test
    void deleteFile_nullObjectKey_throwsResourceNotFoundWithoutCallingS3() {

        // Arrange

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> s3FileStorageService.deleteFile(null));

        // Assert
        assertEquals("File not found while deleting", exception.getMessage());

        verifyNoInteractions(s3Client);
        verifyNoInteractions(s3Presigner);
    }

    @Test
    void deleteFile_emptyObjectKey_throwsResourceNotFoundWithoutCallingS3() {

        // Arrange
        String objectKey = "";

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> s3FileStorageService.deleteFile(objectKey));

        // Assert
        assertEquals("File not found while deleting", exception.getMessage());

        verifyNoInteractions(s3Client);
        verifyNoInteractions(s3Presigner);
    }

    @Test
    void deleteFile_validObjectKey_deletesFromExpectedBucket() {

        // Arrange
        String objectKey = "documents/101/student-id.pdf";

        // Act
        s3FileStorageService.deleteFile(objectKey);

        // Assert
        ArgumentCaptor<DeleteObjectRequest> requestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);

        verify(s3Client).deleteObject(requestCaptor.capture());

        DeleteObjectRequest capturedRequest = requestCaptor.getValue();

        assertEquals(BUCKET_NAME, capturedRequest.bucket());

        assertEquals(objectKey, capturedRequest.key());

        verifyNoInteractions(s3Presigner);
    }

    @Test
    void deleteFile_s3DeleteFails_propagatesSameRuntimeException() {

        // Arrange
        String objectKey = "documents/101/student-id.pdf";

        RuntimeException s3Exception = new RuntimeException("Delete failed");

        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenThrow(s3Exception);

        // Act
        RuntimeException exception = assertThrows(RuntimeException.class, () -> s3FileStorageService.deleteFile(objectKey));

        // Assert
        assertSame(s3Exception, exception);

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));

        verifyNoInteractions(s3Presigner);
    }


    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private PutObjectResponse successfulPutObjectResponse() {

        SdkHttpResponse httpResponse = SdkHttpResponse.builder().statusCode(200).build();

        return (PutObjectResponse) PutObjectResponse.builder().sdkHttpResponse(httpResponse).build();
    }
}