package com.tienda.admin.service;

import com.tienda.admin.model.Administrador;
import com.tienda.admin.repository.AdministradorRepository;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final AdministradorRepository administradorRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    // Simple rate limiting
    private final Map<String, LoginAttempt> loginAttempts = new HashMap<>();
    
    @Transactional
    public LoginResult authenticate(String username, String password) {
        // Sanitizar entrada
        String cleanUsername = sanitizeInput(username);
        String cleanPassword = sanitizeInput(password);
        
        // Verificar rate limiting
        if (isAccountLocked(cleanUsername)) {
            return LoginResult.accountLocked();
        }
        
        // Buscar administrador
        Optional<Administrador> adminOpt = administradorRepository
                .findByUsernameAndActivoTrue(cleanUsername);
        
        if (adminOpt.isEmpty()) {
            recordFailedAttempt(cleanUsername);
            return LoginResult.invalidCredentials();
        }
        
        Administrador admin = adminOpt.get();
        
        // Verificar contraseña
        if (!passwordEncoder.matches(cleanPassword, admin.getPasswordHash())) {
            recordFailedAttempt(cleanUsername);
            return LoginResult.invalidCredentials();
        }
        
        // Login exitoso
        loginAttempts.remove(cleanUsername);
        admin.setLastLogin(LocalDateTime.now());
        administradorRepository.save(admin);
        
        // Generar token simple (en producción usar JWT)
        String token = generateSimpleToken(admin);
        
        return LoginResult.success(token, admin);
    }
    
    private String sanitizeInput(String input) {
        if (input == null) return "";
        return input.trim();
    }
    
    private boolean isAccountLocked(String username) {
        LoginAttempt attempt = loginAttempts.get(username);
        if (attempt == null) return false;
        
        long lockoutTime = 15 * 60 * 1000; // 15 minutos
        return attempt.getAttempts() >= 5 && 
            (System.currentTimeMillis() - attempt.getLastAttempt()) < lockoutTime;
    }
    
    private void recordFailedAttempt(String username) {
        LoginAttempt attempt = loginAttempts.getOrDefault(username, new LoginAttempt());
        attempt.incrementAttempts();
        loginAttempts.put(username, attempt);
    }
    
    private String generateSimpleToken(Administrador admin) {
        // Token simple: username + timestamp + random
        return Base64.getEncoder().encodeToString(
            (admin.getUsername() + ":" + System.currentTimeMillis() + ":" + UUID.randomUUID())
                .getBytes()
        );
    }
    
    public boolean validateToken(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            String[] parts = decoded.split(":");
            return parts.length == 3; // Validar formato
        } catch (Exception e) {
            return false;
        }
    }
    
    // Clases internas
    @Data
    public static class LoginResult {
        private boolean success;
        private String token;
        private Administrador admin;
        private String message;
        private boolean locked;
        
        public static LoginResult success(String token, Administrador admin) {
            LoginResult result = new LoginResult();
            result.success = true;
            result.token = token;
            result.admin = admin;
            result.message = "Login exitoso";
            return result;
        }
        
        public static LoginResult invalidCredentials() {
            LoginResult result = new LoginResult();
            result.success = false;
            result.message = "Credenciales incorrectas";
            return result;
        }
        
        public static LoginResult accountLocked() {
            LoginResult result = new LoginResult();
            result.success = false;
            result.locked = true;
            result.message = "Cuenta bloqueada temporalmente";
            return result;
        }
    }
    
    @Data
    private static class LoginAttempt {
        private int attempts = 0;
        private long lastAttempt = System.currentTimeMillis();
        
        public void incrementAttempts() {
            this.attempts++;
            this.lastAttempt = System.currentTimeMillis();
        }
    }
}
