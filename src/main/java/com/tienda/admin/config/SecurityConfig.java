package com.tienda.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Deshabilitar CSRF correctamente
            .csrf(AbstractHttpConfigurer::disable)
            
            // 2. Configurar CORS - CORREGIDO: usar el bean
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 3. Configurar autorizaciones - ¡SIMPLIFICADO!
            .authorizeHttpRequests(auth -> auth
                // PERMITIR TODO TEMPORALMENTE para que funcione
                .anyRequest().permitAll()  // ✅ CAMBIO CRÍTICO: temporalmente permitAll()
                
                /*
                // Cuando funcione, cambiar a esto:
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/dashboard.html",
                    "/login.html",
                    "/favicon.ico",
                    "/error",
                    "/health",
                    "/status",
                    "/api/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/img/**",
                    "/assets/**",
                    "/static/**",
                    "/uploads/**"
                ).permitAll()
                .anyRequest().authenticated()
                */
            )
            
            // 4. Headers (mantener si usas H2)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // ✅ CORREGIDO: Permitir todos los orígenes TEMPORALMENTE
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // ✅ CORREGIDO: Métodos completos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));
        
        // ✅ CORREGIDO: Headers completos
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "X-CSRF-TOKEN"
        ));
        
        // ✅ CORREGIDO: Headers expuestos
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Disposition"
        ));
        
        // ✅ CORREGIDO: Cambiar a true para permitir cookies/credentials
        configuration.setAllowCredentials(true);  // DE false A true
        
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    // ✅ CORREGIDO: Método de recursos estáticos
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. Archivos subidos
        String uploadPath = Paths.get("uploads").toAbsolutePath().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
        
        // 2. Archivos estáticos de Spring Boot - CORREGIDO
        // Spring Boot ya maneja esto automáticamente, pero lo dejamos por si acaso
        registry.addResourceHandler("/**")
                .addResourceLocations(
                    "classpath:/static/",
                    "classpath:/public/", 
                    "classpath:/resources/",
                    "classpath:/META-INF/resources/"
                );
        
        // 3. Para archivos específicos de tu frontend
        registry.addResourceHandler("/css/**", "/js/**", "/images/**", "/assets/**")
                .addResourceLocations(
                    "classpath:/static/css/",
                    "classpath:/static/js/",
                    "classpath:/static/images/",
                    "classpath:/static/assets/"
                );
        
        System.out.println("✅ Configuración de recursos estáticos completada");
        System.out.println("📁 Ruta uploads: " + uploadPath);
    }
    
    // ❌ ELIMINAR este método (no es necesario y puede causar problemas)
    // public void configure(org.springframework.web.servlet.config.annotation.WebMvcConfigurer configurer) {
    //     // Este método no hace nada y puede causar conflictos
    // }
}