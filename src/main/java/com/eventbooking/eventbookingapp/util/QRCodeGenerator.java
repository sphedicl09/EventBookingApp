package com.eventbooking.eventbookingapp.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.nio.file.*;

public class QRCodeGenerator {

    public static String generateQRCode(String data, String fileName) {
        String filePath = System.getProperty("user.home") + "/Desktop/" + fileName + "_QR.png";
        try {
            BitMatrix matrix = new MultiFormatWriter()
                    .encode(data, BarcodeFormat.QR_CODE, 200, 200);
            Path path = Paths.get(filePath);
            MatrixToImageWriter.writeToPath(matrix, "PNG", path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }
}
