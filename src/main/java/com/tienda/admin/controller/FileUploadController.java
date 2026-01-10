package com.tienda.admin.controller;

import com.tienda.admin.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {
    
    @Autowired
    private FileUploadService fileUploadService;
    
    @PostMapping("/imagen")
    public ResponseEntity<?> subirImagen(@RequestParam("file") MultipartFile archivo) {
        try {
            if (archivo.isEmpty()) {
                return ResponseEntity.badRequest().body("No se envió ningún archivo");
            }
            
            String rutaRelativa = fileUploadService.guardarImagen(archivo);
            String baseUrl = "http://localhost:8080";
            String urlCompleta = baseUrl + rutaRelativa;
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", "Imagen subida correctamente");
            respuesta.put("ruta", rutaRelativa);
            respuesta.put("url", urlCompleta);
            respuesta.put("nombre", archivo.getOriginalFilename());
            respuesta.put("tamaño", archivo.getSize());
            respuesta.put("tipo", archivo.getContentType());
            
            return ResponseEntity.ok(respuesta);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("success", false, "message", "Error al subir imagen: " + e.getMessage())
            );
        }
    }
    
    @DeleteMapping("/imagen")
    public ResponseEntity<?> eliminarImagen(@RequestParam String ruta) {
        try {
            boolean eliminado = fileUploadService.eliminarImagen(ruta);
            
            if (eliminado) {
                return ResponseEntity.ok(
                    Map.of("success", true, "message", "Imagen eliminada correctamente")
                );
            } else {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "No se pudo eliminar la imagen")
                );
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("success", false, "message", "Error al eliminar imagen")
            );
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<?> obtenerStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "File Upload Service");
        status.put("status", "running");
        status.put("uploadDirectory", fileUploadService.getUploadDirectory());
        status.put("message", "Servicio de imágenes funcionando correctamente");
        
        return ResponseEntity.ok(status);
    }
}
