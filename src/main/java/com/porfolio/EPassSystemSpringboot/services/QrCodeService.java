package com.porfolio.EPassSystemSpringboot.services;

public interface QrCodeService {

    byte[] generateQrCode(String content, int width, int height);

    String decodeQrCode(byte[] imageBytes);

}
