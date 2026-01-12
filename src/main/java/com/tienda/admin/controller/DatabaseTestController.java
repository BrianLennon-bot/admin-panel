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
import java.util.*;

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
            response.put("timestamp", new Date());
            response.put("url", safeGetUrl(conn));
            response.put("database", safeGetCatalog(conn));
            response.put("username", safeGetUsername(metaData));
            response.put("driver", metaData.getDriverName());
            response.put("driver_version", metaData.getDriverVersion());
            response.put("database_version", metaData.getDatabaseProductVersion());
            
            // Listar tablas
            List<String> tables = getTableList(metaData);
            response.put("tables", tables);
            response.put("table_count", tables.size());
            
            // Verificar tabla 'administradores'
            if (tables.contains("administradores")) {
                response.put("administradores_table", "EXISTS");
                try {
                    var stmt = conn.createStatement();
                    var countRs = stmt.executeQuery("SELECT COUNT(*) as count FROM administradores");
                    if (countRs.next()) {
                        response.put("administradores_count", countRs.getInt("count"));
                    }
                } catch (SQLException e) {
                    response.put("administradores_count_error", "No se pudo contar: " + e.getMessage());
                }
            } else {
                response.put("administradores_table", "NOT_FOUND");
                response.put("suggestion", "Usa spring.jpa.hibernate.ddl-auto=update para crear tablas automáticamente");
            }
            
            // Verificar otras tablas importantes
            checkImportantTables(tables, response);
            
        } catch (SQLException e) {
            response.put("status", "ERROR");
            response.put("timestamp", new Date());
            response.put("error", e.getMessage());
            response.put("sql_state", e.getSQLState());
            response.put("error_code", e.getErrorCode());
            response.put("error_class", e.getClass().getName());
            
            // Información adicional de diagnóstico
            response.put("data_source_class", dataSource.getClass().getName());
            response.put("suggestions", Arrays.asList(
                "Verifica spring.datasource.url en application.properties",
                "Verifica que la BD de Render esté activa",
                "Verifica usuario y contraseña"
            ));
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/api/debug/env")
    public ResponseEntity<?> debugEnvironment() {
        Map<String, Object> env = new HashMap<>();
        env.put("timestamp", new Date());
        
        // Variables de entorno importantes
        env.put("PORT", System.getenv("PORT"));
        env.put("DATABASE_URL", safeGetEnv("DATABASE_URL"));
        env.put("JDBC_DATABASE_URL", safeGetEnv("JDBC_DATABASE_URL"));
        env.put("JDBC_DATABASE_USERNAME", safeGetEnv("JDBC_DATABASE_USERNAME"));
        env.put("JDBC_DATABASE_PASSWORD", safeGetEnv("JDBC_DATABASE_PASSWORD") != null ? "PRESENT" : "NOT_PRESENT");
        
        // Propiedades del sistema
        env.put("java_version", System.getProperty("java.version"));
        env.put("java_vendor", System.getProperty("java.vendor"));
        env.put("os_name", System.getProperty("os.name"));
        
        // Información de la aplicación
        env.put("spring_version", org.springframework.core.SpringVersion.getVersion());
        
        // Configuración actual del DataSource (si está disponible)
        Map<String, Object> config = new HashMap<>();
        try {
            config.put("data_source_class", dataSource.getClass().getName());
            
            try (Connection conn = dataSource.getConnection()) {
                config.put("connection_status", "SUCCESS");
                config.put("db_url", conn.getMetaData().getURL());
                config.put("db_user", conn.getMetaData().getUserName());
                config.put("db_name", conn.getCatalog());
            } catch (SQLException e) {
                config.put("connection_status", "FAILED");
                config.put("connection_error", e.getMessage());
            }
        } catch (Exception e) {
            config.put("error", "No se pudo obtener información del DataSource: " + e.getMessage());
        }
        
        env.put("datasource_info", config);
        
        return ResponseEntity.ok(env);
    }
    
    @GetMapping("/api/debug/ping")
    public ResponseEntity<?> pingDatabase() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", new Date());
        
        try (Connection conn = dataSource.getConnection()) {
            // Query simple para verificar conexión
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery("SELECT 1 as test, current_timestamp as server_time");
            
            if (rs.next()) {
                response.put("status", "OK");
                response.put("database", "ALIVE");
                response.put("test_result", rs.getInt("test"));
                response.put("server_time", rs.getTimestamp("server_time"));
                response.put("response_time_ms", System.currentTimeMillis() - ((Date)response.get("timestamp")).getTime());
            }
            
        } catch (SQLException e) {
            response.put("status", "ERROR");
            response.put("database", "UNREACHABLE");
            response.put("error", e.getMessage());
            response.put("sql_state", e.getSQLState());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/api/debug/create-test-admin")
    public ResponseEntity<?> createTestAdmin() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", new Date());
        
        try (Connection conn = dataSource.getConnection()) {
            // Verificar si la tabla existe
            var checkStmt = conn.createStatement();
            var rs = checkStmt.executeQuery(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'administradores')"
            );
            
            if (rs.next() && rs.getBoolean(1)) {
                // Tabla existe, intentar insertar admin de prueba
                String insertSQL = """
                    INSERT INTO administradores (username, password, nombre, email, activo, created_at, updated_at) 
                    VALUES ('admin', '$2a$10$XH6yZYKGpN8wK5q2v7QYDeEXAMPLEHASH', 'Admin Test', 'admin@test.com', true, NOW(), NOW())
                    ON CONFLICT (username) DO NOTHING
                    RETURNING id, username
                    """;
                
                var insertStmt = conn.createStatement();
                var insertRs = insertStmt.executeQuery(insertSQL);
                
                if (insertRs.next()) {
                    response.put("status", "CREATED");
                    response.put("admin_id", insertRs.getLong("id"));
                    response.put("username", insertRs.getString("username"));
                    response.put("message", "Admin de prueba creado: admin / admin123");
                } else {
                    response.put("status", "EXISTS");
                    response.put("message", "El admin 'admin' ya existe");
                }
            } else {
                response.put("status", "NO_TABLE");
                response.put("message", "La tabla 'administradores' no existe");
                response.put("suggestion", "Cambia spring.jpa.hibernate.ddl-auto a 'update' o 'create'");
            }
            
        } catch (SQLException e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            response.put("sql_state", e.getSQLState());
        }
        
        return ResponseEntity.ok(response);
    }
    
    // ========== MÉTODOS PRIVADOS DE UTILIDAD ==========
    
    private List<String> getTableList(DatabaseMetaData metaData) {
        List<String> tables = new ArrayList<>();
        try {
            ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            tables.add("Error al listar tablas: " + e.getMessage());
        }
        return tables;
    }
    
    private void checkImportantTables(List<String> tables, Map<String, Object> response) {
        List<String> importantTables = Arrays.asList("administradores", "productos", "usuarios", "categorias");
        Map<String, Boolean> tableStatus = new HashMap<>();
        
        for (String table : importantTables) {
            tableStatus.put(table, tables.contains(table.toLowerCase()) || tables.contains(table.toUpperCase()));
        }
        response.put("important_tables", tableStatus);
    }
    
    private String safeGetUrl(Connection conn) {
        try {
            return conn.getMetaData().getURL();
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }
    
    private String safeGetCatalog(Connection conn) {
        try {
            return conn.getCatalog();
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }
    
    private String safeGetUsername(DatabaseMetaData metaData) {
        try {
            return metaData.getUserName();
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }
    
    private String safeGetEnv(String key) {
        String value = System.getenv(key);
        if (value == null) return "NOT_PRESENT";
        if (key.contains("PASSWORD")) return "PRESENT (hidden)";
        return value;
    }
}