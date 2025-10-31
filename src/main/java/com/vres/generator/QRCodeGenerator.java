package com.vres.generator;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// Utility class for converting text to a QR code image byte array.
public class QRCodeGenerator {

    public static byte[] generateQRCodeImage(String text, int width, int height) 
            throws WriterException, IOException {
        
        // Configuration hints for robustness
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // High error correction
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8"); 

        // Encode the text into a BitMatrix
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
            text, 
            BarcodeFormat.QR_CODE, 
            width, 
            height,
            hints
        );

        // Write the matrix to a PNG byte stream
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(
            bitMatrix, 
            "PNG", 
            pngOutputStream
        );
        
        return pngOutputStream.toByteArray();
    }
}
