package com.tienda.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.io.FilenameUtils;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileUploadService {
    
    private final Path uploadPath = Paths.get("uploads");
    
    @PostConstruct
    public void init() {
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("✅ Directorio 'uploads' creado en: " + uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("⚠️  No se pudo crear el directorio 'uploads': " + e.getMessage());
        }
    }
    
    public String guardarImagen(MultipartFile archivo) throws IOException {
        validarArchivo(archivo);
        
        String nombreOriginal = archivo.getOriginalFilename();
        String extension = FilenameUtils.getExtension(nombreOriginal).toLowerCase();
        String nombreUnico = UUID.randomUUID().toString() + "." + extension;
        
        Path destino = uploadPath.resolve(nombreUnico);
        Files.copy(archivo.getInputStream(), destino);
        
        System.out.println("✅ Imagen guardada: " + nombreUnico);
        
        return "/uploads/" + nombreUnico;
    }
    
    private void validarArchivo(MultipartFile archivo) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        
        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo no es una imagen válida");
        }
        
        String extension = FilenameUtils.getExtension(archivo.getOriginalFilename()).toLowerCase();
        String[] extensionesValidas = {"jpg", "jpeg", "png", "gif", "webp", "bmp"};
        
        boolean esValida = false;
        for (String ext : extensionesValidas) {
            if (ext.equals(extension)) {
                esValida = true;
                break;
            }
        }
        
        if (!esValida) {
            throw new IllegalArgumentException("Formato no soportado. Use: JPG, PNG, GIF, WEBP, BMP");
        }
        
        long maxSize = 5 * 1024 * 1024;
        if (archivo.getSize() > maxSize) {
            throw new IllegalArgumentException("La imagen es demasiado grande. Máximo 5MB");
        }
    }
    
    public boolean eliminarImagen(String rutaImagen) {
        if (rutaImagen == null || !rutaImagen.startsWith("/uploads/")) {
            return false;
        }
        
        try {
            String nombreArchivo = rutaImagen.substring(rutaImagen.lastIndexOf("/") + 1);
            Path archivoPath = uploadPath.resolve(nombreArchivo);
            
            if (Files.exists(archivoPath)) {
                Files.delete(archivoPath);
                System.out.println("✅ Imagen eliminada: " + nombreArchivo);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error eliminando imagen: " + e.getMessage());
            return false;
        }
    }
    
    public String getUploadDirectory() {
        return uploadPath.toAbsolutePath().toString();
    }
}
