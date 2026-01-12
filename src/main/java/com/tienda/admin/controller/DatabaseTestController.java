package com.tienda.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class DatabaseTestController {
    
    @Autowired
    private DataSource dataSource;
    
    @GetMapping("/api/debug/db")
    public ResponseEntity<?> debugDatabase() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            // Información de conexión
            response.put("status", "CONNECTED");
            response.put("url", conn.getMetaData().getURL());
            response.put("database", conn.getCatalog());
            response.put("username", metaData.getUserName());
            response.put("driver", metaData.getDriverName());
            response.put("driver_version", metaData.getDriverVersion());
            
            // Listar tablas
            List<String> tables = new ArrayList<>();
            ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
            response.put("tables", tables);
            response.put("table_count", tables.size());
            
            // Verificar tabla 'administradores'
            if (tables.contains("administradores")) {
                response.put("administradores_table", "EXISTS");
                // Contar registros
                var stmt = conn.createStatement();
                var countRs = stmt.executeQuery("SELECT COUNT(*) as count FROM administradores");
                if (countRs.next()) {
                    response.put("administradores_count", countRs.getInt("count"));
                }
            } else {
                response.put("administradores_table", "NOT_FOUND");
            }
            
        } catch (SQLException e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            response.put("sql_state", e.getSQLState());
            response.put("error_code", e.getErrorCode());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/api/debug/env")
    public ResponseEntity<?> debugEnvironment() {
        Map<String, Object> env = new HashMap<>();
        
        // Variables de entorno importantes
        env.put("PORT", System.getenv("PORT"));
        env.put("DATABASE_URL", System.getenv("DATABASE_URL") != null ? "PRESENT" : "NOT_PRESENT");
        env.put("java_version", System.getProperty("java.version"));
        
        // Configuración actual
        Map<String, String> config = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            config.put("db_url", conn.getMetaData().getURL());
            config.put("db_user", conn.getMetaData().getUserName());
        } catch (Exception e) {
            config.put("error", e.getMessage());
        }
        env.put("current_config", config);
        
        return ResponseEntity.ok(env);
    }
}