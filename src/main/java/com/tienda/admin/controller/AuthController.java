package com.tienda.admin.controller;

import com.tienda.admin.dto.LoginRequest;
import com.tienda.admin.dto.LoginResponse;
import com.tienda.admin.model.Administrador;
import com.tienda.admin.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin-api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://127.0.0.1:5500", "http://127.0.0.1:8080"})
public class AuthController {
    
    private final AuthService authService;
    
    private void setCorsHeaders(HttpServletResponse response, String origin) {
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, 
                                   HttpServletRequest httpRequest,
                                   HttpServletResponse response) {
        String origin = httpRequest.getHeader("Origin");
        System.out.println("=== INTENTO DE LOGIN ===");
        System.out.println("Usuario: " + request.getUsername());
        System.out.println("Origen: " + origin);
        System.out.println("IP: " + httpRequest.getRemoteAddr());
        
        // Set CORS headers basados en el origen de la petición
        if (origin != null && (origin.contains("localhost") || origin.contains("127.0.0.1"))) {
            setCorsHeaders(response, origin);
        } else {
            setCorsHeaders(response, "http://127.0.0.1:5500");
        }
        
        try {
            var result = authService.authenticate(request.getUsername(), request.getPassword());
            
            if (result.isSuccess()) {
                Administrador admin = result.getAdmin();
                
                LoginResponse.AdminInfo adminInfo = new LoginResponse.AdminInfo();
                adminInfo.setId(admin.getId());
                adminInfo.setUsername(admin.getUsername());
                adminInfo.setNombre(admin.getNombre());
                adminInfo.setEmail(admin.getEmail());
                
                LoginResponse loginResponse = new LoginResponse();
                loginResponse.setSuccess(true);
                loginResponse.setMessage("✅ Inicio de sesión exitoso");
                loginResponse.setToken(result.getToken());
                loginResponse.setAdmin(adminInfo);
                
                System.out.println("✅ Login exitoso para: " + admin.getUsername());
                
                return ResponseEntity.ok(loginResponse);
                
            } else if (result.isLocked()) {
                System.out.println("⛔ Cuenta bloqueada: " + request.getUsername());
                return ResponseEntity.status(423).body(Map.of(
                    "success", false,
                    "message", result.getMessage(),
                    "locked", true
                ));
            } else {
                System.out.println("❌ Credenciales incorrectas: " + request.getUsername());
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", result.getMessage()
                ));
            }
            
        } catch (Exception e) {
            System.err.println("🔥 Error en login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error interno del servidor"
            ));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<?> health(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (origin != null && (origin.contains("localhost") || origin.contains("127.0.0.1"))) {
            setCorsHeaders(response, origin);
        }
        
        Map<String, Object> healthResponse = new HashMap<>();
        healthResponse.put("status", "OK");
        healthResponse.put("service", "admin-panel-auth");
        healthResponse.put("timestamp", new Date().toString());
        healthResponse.put("cors", "enabled");
        healthResponse.put("allowed_origins", new String[]{"http://localhost:5500", "http://127.0.0.1:5500"});
        healthResponse.put("current_origin", origin);
        
        return ResponseEntity.ok(healthResponse);
    }
    
    @GetMapping("/test")
    public ResponseEntity<?> test(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (origin != null && (origin.contains("localhost") || origin.contains("127.0.0.1"))) {
            setCorsHeaders(response, origin);
        }
        
        Map<String, Object> testResponse = new HashMap<>();
        testResponse.put("message", "✅ Backend funcionando correctamente");
        testResponse.put("timestamp", new Date().toString());
        testResponse.put("endpoint", "/admin-api/auth/test");
        testResponse.put("cors", "true");
        testResponse.put("origin", origin);
        
        return ResponseEntity.ok(testResponse);
    }
    
    // Endpoint para manejar preflight requests (OPTIONS)
    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (origin != null && (origin.contains("localhost") || origin.contains("127.0.0.1"))) {
            setCorsHeaders(response, origin);
        }
        return ResponseEntity.ok().build();
    }
}