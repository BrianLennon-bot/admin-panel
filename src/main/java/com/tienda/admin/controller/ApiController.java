package com.tienda.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    
    @GetMapping("/welcome")
    public ResponseEntity<?> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "✅ Backend Spring Boot funcionando");
        response.put("service", "Tienda Admin Panel");
        response.put("timestamp", new Date().toString());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of(
            "status", "ok",
            "message", "API funcionando"
        ));
    }
    
    // Endpoint catch-all para APIs no encontradas
    @RequestMapping("/**")
    public ResponseEntity<?> catchAllApi() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Endpoint API no encontrado");
        response.put("timestamp", new Date().toString());
        return ResponseEntity.status(404).body(response);
    }
}