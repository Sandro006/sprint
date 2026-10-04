package util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import jakarta.servlet.http.Part;

public class CustomPart {
    private String fileName;
    private byte[] bytes;

    // Constructeur par défaut
    public CustomPart() {
    }

    // Constructeur qui accepte un objet Part
    public CustomPart(Part part) throws IOException {
        this.fileName = extractFileName(part);
        this.bytes = extractBytes(part);
    }

    // Méthode pour extraire le nom de fichier depuis le Part
    private String extractFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        for (String token : contentDisposition.split(";")) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf('=') + 2, token.length() - 1);
            }
        }
        return null;
    }

    // Méthode pour extraire les octets depuis le Part
    private byte[] extractBytes(Part part) throws IOException {
        try (InputStream inputStream = part.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        }
    }

    // Getters et Setters
    public String getFileName() {
        return fileName;
    }

    public byte[] getBytes() {
        return bytes;
    }

    // Méthode toString pour afficher l'objet
    @Override
    public String toString() {
        return "CustomPart{" +
                "fileName='" + fileName + '\'' +
                ", bytesLength=" + (bytes != null ? bytes.length : 0) +
                '}';
    }
}
