package com.tienda.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ApiController {
    
    // Endpoint para todas las rutas no encontradas (para debug)
    @RequestMapping("/api/**")
    public ResponseEntity<?> catchAllApi(@RequestParam(required = false) String path) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Endpoint no encontrado");
        response.put("message", "El endpoint que buscas no existe o está mal escrito");
        response.put("suggestions", new String[]{
            "/api/auth/test",
            "/api/auth/login", 
            "/api/test",
            "/api/health"
        });
        response.put("timestamp", new Date().toString());
        
        return ResponseEntity.status(404).body(response);
    }
    
    // Endpoint de bienvenida para la raíz
    @GetMapping("/")
    public ResponseEntity<?> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "✅ Backend Spring Boot funcionando");
        response.put("service", "Tienda Admin Panel");
        response.put("version", "1.0.0");
        response.put("timestamp", new Date().toString());
        response.put("endpoints", new String[]{
            "GET  /api/auth/test",
            "POST /api/auth/login",
            "GET  /api/test", 
            "GET  /api/health"
        });
        
        return ResponseEntity.ok(response);
    }
}