package com.example.desarrollo_aplicaciones.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.example.desarrollo_aplicaciones.service.QRCodeService;

@RestController
@RequestMapping("/api/qr")
public class QRCodeController {

    @Autowired
    private QRCodeService qrCodeService;

    @GetMapping("/generate")
    public String generateQRCode(@RequestParam("text") String text) {
        return qrCodeService.generateQRCodeBase64(text, 300, 300);
    }

    @GetMapping("/view")
    public ResponseEntity<String> viewQRCode(@RequestParam("text") String text) {
        String base64 = qrCodeService.generateQRCodeBase64(text, 300, 300);
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>QR de entrega</title>
            </head>
            <body>
                <h1>QR de la entrega</h1>
                <img src="data:image/png;base64,%s" alt="QR">
            </body>
            </html>
            """.formatted(base64);

        return ResponseEntity.ok()
            .header("Content-Type", "text/html")
            .body(html);
    }
}
