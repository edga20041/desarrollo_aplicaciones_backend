package com.example.desarrollo_aplicaciones.service;

import net.glxn.qrgen.javase.QRCode;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class QRCodeService {

    public String generateQRCodeBase64(String text, int width, int height) {
        ByteArrayOutputStream stream = QRCode
                .from(text)
                .withSize(width, height)
                .stream();

        return Base64.getEncoder().encodeToString(stream.toByteArray());
    }
}
