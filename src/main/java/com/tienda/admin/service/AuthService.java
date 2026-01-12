package com.tienda.admin.service;

import com.tienda.admin.model.Administrador;
import com.tienda.admin.repository.AdministradorRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final AdministradorRepository administradorRepository;
    private final BCryptPasswordEncoder passwordEncoder; // ✅ INYECTADO CORRECTAMENTE
    
    // Simple rate limiting
    private final Map<String, LoginAttempt> loginAttempts = new HashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_TIME_MS = 15 * 60 * 1000; // 15 minutos
    
    @Transactional
    public LoginResult authenticate(String username, String password) {
        log.info("=== INTENTO DE LOGIN ===");
        log.info("Usuario: {}", username);
        
        // Sanitizar entrada
        String cleanUsername = sanitizeInput(username);
        String cleanPassword = sanitizeInput(password);
        
        // Verificar rate limiting
        if (isAccountLocked(cleanUsername)) {
            log.warn("⛔ Cuenta bloqueada: {}", cleanUsername);
            return LoginResult.accountLocked();
        }
        
        // Buscar administrador
        Optional<Administrador> adminOpt = administradorRepository
                .findByUsernameAndActivoTrue(cleanUsername);
        
        if (adminOpt.isEmpty()) {
            log.warn("❌ Usuario no encontrado: {}", cleanUsername);
            recordFailedAttempt(cleanUsername);
            return LoginResult.invalidCredentials();
        }
        
        Administrador admin = adminOpt.get();
        log.info("✅ Usuario encontrado en DB: {}", admin.getUsername());
        log.info("🔑 Hash en DB: {}", admin.getPasswordHash());
        
        // DEBUG: Verificar formato del hash
        if (admin.getPasswordHash() == null || admin.getPasswordHash().isEmpty()) {
            log.error("⚠️ El hash de contraseña está vacío o nulo");
            return LoginResult.invalidCredentials();
        }
        
        // Verificar formato BCrypt
        if (!admin.getPasswordHash().startsWith("$2a$") && 
            !admin.getPasswordHash().startsWith("$2b$") &&
            !admin.getPasswordHash().startsWith("$2y$")) {
            log.error("⚠️ El hash no tiene formato BCrypt: {}", admin.getPasswordHash());
            return LoginResult.invalidCredentials();
        }
        
        // Verificar contraseña CON DEBUG
        log.info("🔐 Comparando contraseña recibida con hash...");
        boolean passwordMatches = false;
        
        try {
            // Método 1: Usando passwordEncoder
            passwordMatches = passwordEncoder.matches(cleanPassword, admin.getPasswordHash());
            log.info("📊 Resultado passwordEncoder.matches(): {}", passwordMatches);
            
            // Método 2: Usando BCrypt directamente para comparar
            if (!passwordMatches) {
                log.info("🔄 Intentando con BCrypt.checkpw()...");
                passwordMatches = BCrypt.checkpw(cleanPassword, admin.getPasswordHash());
                log.info("📊 Resultado BCrypt.checkpw(): {}", passwordMatches);
            }
            
        } catch (Exception e) {
            log.error("🔥 Error al verificar contraseña: {}", e.getMessage(), e);
        }
        
        if (!passwordMatches) {
            log.warn("❌ Contraseña incorrecta para usuario: {}", cleanUsername);
            recordFailedAttempt(cleanUsername);
            
            // DEBUG: Generar un hash con la contraseña recibida para comparar
            String testHash = passwordEncoder.encode(cleanPassword);
            log.info("🔍 Hash generado con contraseña recibida: {}", testHash);
            log.info("🔍 Hash almacenado en DB: {}", admin.getPasswordHash());
            
            return LoginResult.invalidCredentials();
        }
        
        // Login exitoso
        log.info("✅ Login exitoso para: {}", admin.getUsername());
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
        
        long timeSinceLastAttempt = System.currentTimeMillis() - attempt.getLastAttempt();
        boolean isLocked = attempt.getAttempts() >= MAX_ATTEMPTS && timeSinceLastAttempt < LOCKOUT_TIME_MS;
        
        if (isLocked) {
            long remainingMinutes = (LOCKOUT_TIME_MS - timeSinceLastAttempt) / (60 * 1000);
            log.warn("🔒 Cuenta {} bloqueada. Tiempo restante: {} minutos", 
                    username, remainingMinutes + 1);
        }
        
        // Resetear intentos si ya pasó el tiempo de bloqueo
        if (timeSinceLastAttempt >= LOCKOUT_TIME_MS) {
            loginAttempts.remove(username);
            return false;
        }
        
        return isLocked;
    }
    
    private void recordFailedAttempt(String username) {
        LoginAttempt attempt = loginAttempts.get(username);
        if (attempt == null) {
            attempt = new LoginAttempt();
        }
        attempt.incrementAttempts();
        loginAttempts.put(username, attempt);
        
        int remainingAttempts = MAX_ATTEMPTS - attempt.getAttempts();
        log.warn("⚠️ Intento fallido para {}. Intentos restantes: {}", 
                username, Math.max(0, remainingAttempts));
    }
    
    private String generateSimpleToken(Administrador admin) {
        // Token simple: username + timestamp + random
        String tokenData = admin.getUsername() + ":" + 
                          System.currentTimeMillis() + ":" + 
                          UUID.randomUUID();
        return Base64.getEncoder().encodeToString(tokenData.getBytes());
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
    
    // Método para resetear contraseña (para pruebas)
    @Transactional
    public boolean resetPassword(String username, String newPassword) {
        Optional<Administrador> adminOpt = administradorRepository
                .findByUsernameAndActivoTrue(username);
        
        if (adminOpt.isPresent()) {
            Administrador admin = adminOpt.get();
            String hashedPassword = passwordEncoder.encode(newPassword);
            log.info("🔄 Reseteando contraseña para: {}", username);
            log.info("Nuevo hash: {}", hashedPassword);
            
            admin.setPasswordHash(hashedPassword);
            administradorRepository.save(admin);
            return true;
        }
        
        return false;
    }
    
    // Método para verificar hash existente
    public boolean verifyPassword(String rawPassword, String storedHash) {
        try {
            return passwordEncoder.matches(rawPassword, storedHash) ||
                   BCrypt.checkpw(rawPassword, storedHash);
        } catch (Exception e) {
            log.error("Error verificando contraseña: {}", e.getMessage());
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
            result.message = "Cuenta bloqueada temporalmente por múltiples intentos fallidos. Intente en 15 minutos.";
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