package com.tienda.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")  // ✅ Ruta base /api
public class ApiController {
    
    @GetMapping("/welcome")
    public ResponseEntity<?> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "✅ Backend Spring Boot funcionando");
        response.put("service", "Tienda Admin Panel");
        response.put("timestamp", new Date().toString());
        return ResponseEntity.ok(response);
    }
    
    // ✅ ÚNICO método /test en toda la aplicación
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "API funcionando");
        response.put("timestamp", new Date().toString());
        response.put("endpoint", "/api/test");
        return ResponseEntity.ok(response);
    }
    
    // ✅ ÚNICO método /health en toda la aplicación
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("service", "admin-panel-api");
        response.put("timestamp", new Date().toString());
        return ResponseEntity.ok(response);
    }
    
    // Endpoint catch-all para APIs no encontradas
    @RequestMapping("/**")
    public ResponseEntity<?> catchAllApi() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Endpoint API no encontrado");
        response.put("timestamp", new Date().toString());
        response.put("suggestions", new String[]{
            "GET  /api/welcome",
            "GET  /api/test", 
            "GET  /api/health",
            "GET  /api/auth/test",
            "POST /api/auth/login"
        });
        return ResponseEntity.status(404).body(response);
    }
}