package com.porfolio.EPassSystemSpringboot.services;


import com.porfolio.EPassSystemSpringboot.exceptions.BusinessException;
import com.porfolio.EPassSystemSpringboot.services.implementations.QrCodeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeServiceImplTest {

    private static final String QR_TOKEN = "550e8400-e29b-41d4-a716-446655440000";

    private QrCodeServiceImpl qrCodeService;

    @BeforeEach
    void setUp() {

        // Arrange
        qrCodeService = new QrCodeServiceImpl();
    }


    // -------------------------------------------------------------------------
    // generateQrCode
    // -------------------------------------------------------------------------

    @Test
    void generateQrCode_validContent_returnsPngBytes() throws IOException {

        // Arrange
        int width = 300;
        int height = 300;

        // Act
        byte[] result = qrCodeService.generateQrCode(QR_TOKEN, width, height);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(result));

        assertNotNull(image);

        assertEquals(width, image.getWidth());

        assertEquals(height, image.getHeight());
    }

    @Test
    void generateQrCode_validContent_returnsPngFormattedImage() {

        // Arrange
        byte[] expectedPngSignature = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

        // Act
        byte[] result = qrCodeService.generateQrCode(QR_TOKEN, 300, 300);

        // Assert
        assertTrue(result.length >= expectedPngSignature.length);

        byte[] actualSignature = new byte[expectedPngSignature.length];

        System.arraycopy(result, 0, actualSignature, 0, expectedPngSignature.length);

        assertArrayEquals(expectedPngSignature, actualSignature);
    }

    @Test
    void generateQrCode_thenDecode_returnsOriginalContent() {

        // Arrange
        String content = "PASS-QR-TOKEN-123456789";

        // Act
        byte[] qrCode = qrCodeService.generateQrCode(content, 300, 300);

        String decoded = qrCodeService.decodeQrCode(qrCode);

        // Assert
        assertEquals(content, decoded);
    }

    @Test
    void generateQrCode_differentContents_generateDifferentImages() {

        // Arrange
        String firstContent = "TOKEN-ONE";

        String secondContent = "TOKEN-TWO";

        // Act
        byte[] firstQr = qrCodeService.generateQrCode(firstContent, 300, 300);

        byte[] secondQr = qrCodeService.generateQrCode(secondContent, 300, 300);

        // Assert
        assertFalse(java.util.Arrays.equals(firstQr, secondQr));

        assertEquals(firstContent, qrCodeService.decodeQrCode(firstQr));

        assertEquals(secondContent, qrCodeService.decodeQrCode(secondQr));
    }

    @Test
    void generateQrCode_emptyContent_throwsBusinessException() {

        // Arrange
        String content = "";

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> qrCodeService.generateQrCode(content, 300, 300));

        // Assert
        assertEquals("Found empty contents", exception.getMessage());
    }

    // -------------------------------------------------------------------------
    // decodeQrCode
    // -------------------------------------------------------------------------

    @Test
    void decodeQrCode_validQrImage_returnsDecodedToken() {

        // Arrange
        byte[] qrCode = qrCodeService.generateQrCode(QR_TOKEN, 300, 300);

        // Act
        String result = qrCodeService.decodeQrCode(qrCode);

        // Assert
        assertEquals(QR_TOKEN, result);
    }

    @Test
    void decodeQrCode_nonImageBytes_throwsUnsupportedImageBusinessException() {

        // Arrange
        byte[] invalidImage = "this-is-not-an-image".getBytes(StandardCharsets.UTF_8);

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> qrCodeService.decodeQrCode(invalidImage));

        // Assert
        assertEquals("Uploaded file is not a supported image", exception.getMessage());
    }

    @Test
    void decodeQrCode_validImageWithoutQr_throwsNoReadableQrBusinessException() throws IOException {

        // Arrange
        BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ImageIO.write(image, "PNG", outputStream);

        byte[] imageBytes = outputStream.toByteArray();

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> qrCodeService.decodeQrCode(imageBytes));

        // Assert
        assertEquals("No readable QR code found in the uploaded image", exception.getMessage());
    }

    @Test
    void decodeQrCode_emptyByteArray_throwsUnsupportedImageBusinessException() {

        // Arrange
        byte[] emptyImage = new byte[0];

        // Act
        BusinessException exception = assertThrows(BusinessException.class, () -> qrCodeService.decodeQrCode(emptyImage));

        // Assert
        assertEquals("Uploaded file is not a supported image", exception.getMessage());
    }

    @Test
    void decodeQrCode_nullByteArray_currentBehaviorThrowsNullPointerException() {

        // Arrange

        // Act
        assertThrows(NullPointerException.class, () -> qrCodeService.decodeQrCode(null));

        // Assert
    }
}
