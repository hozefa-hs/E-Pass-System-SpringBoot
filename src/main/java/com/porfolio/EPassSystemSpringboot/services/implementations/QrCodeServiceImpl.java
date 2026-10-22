package com.porfolio.EPassSystemSpringboot.services.implementations;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.porfolio.EPassSystemSpringboot.exceptions.BusinessException;
import com.porfolio.EPassSystemSpringboot.services.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class QrCodeServiceImpl implements QrCodeService {

    @Override
    public byte[] generateQrCode(String content, int width, int height) {

        try {

            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);

            return outputStream.toByteArray();

        } catch (WriterException | IOException exception) {
            throw new BusinessException("Unable to generate Qr code");
        }

    }

    @Override
    public String decodeQrCode(byte[] imageBytes) {
        try (ByteArrayInputStream inputStream =
                     new ByteArrayInputStream(imageBytes)) {

            BufferedImage image = ImageIO.read(inputStream);

            if (image == null) {
                throw new BusinessException(
                        "Uploaded file is not a supported image"
                );
            }

            BinaryBitmap bitmap = new BinaryBitmap(
                    new HybridBinarizer(
                            new BufferedImageLuminanceSource(image)
                    )
            );

            Result result = new MultiFormatReader().decode(bitmap);

            String decodedText = result.getText();

            if (decodedText == null || decodedText.isBlank()) {
                throw new BusinessException(
                        "QR code does not contain a token"
                );
            }

            return decodedText;

        } catch (NotFoundException exception) {
            throw new BusinessException(
                    "No readable QR code found in the uploaded image"
            );
        } catch (IOException exception) {
            throw new BusinessException(
                    "Unable to read the uploaded QR code image"
            );
        }
    }
}
