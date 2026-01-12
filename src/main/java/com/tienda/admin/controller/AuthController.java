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
@RequestMapping("/api/auth")  // ✅ CAMBIADO: Solo /api/auth
@RequiredArgsConstructor
@CrossOrigin(origins = "*")  // ✅ Simplificado
public class AuthController {
    
    private final AuthService authService;
    
    // ✅ Ruta única: /api/auth/test
    @GetMapping("/test")
    public ResponseEntity<?> authTest(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> testResponse = new HashMap<>();
        testResponse.put("success", true);
        testResponse.put("message", "✅ Auth endpoint funcionando correctamente");
        testResponse.put("timestamp", new Date().toString());
        testResponse.put("endpoint", "/api/auth/test");
        
        return ResponseEntity.ok(testResponse);
    }
    
    // ✅ ELIMINADO: El método /test general (va en ApiController)
    // @GetMapping("/test")  // ❌ QUITAR ESTE
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        System.out.println("=== INTENTO DE LOGIN ===");
        System.out.println("Usuario: " + request.getUsername());
        
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
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error interno del servidor"
            ));
        }
    }
    
    // ✅ ELIMINADO: El método /health (va en ApiController)
    // @GetMapping("/health")  // ❌ QUITAR ESTE
    
    // Endpoint para manejar preflight requests (OPTIONS)
    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }
}