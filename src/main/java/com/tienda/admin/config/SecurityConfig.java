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
public class SecurityConfig implements WebMvcConfigurer {  // Implementa directamente

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Deshabilitar CSRF correctamente
            .csrf(AbstractHttpConfigurer::disable)
            
            // 2. Configurar CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 3. Configurar autorizaciones - ¡ESTE ES EL PROBLEMA PRINCIPAL!
            .authorizeHttpRequests(auth -> auth
                // Primero, permitir TODOS los archivos estáticos
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/dashboard.html",
                    "/index",
                    "/favicon.ico",
                    "/error",
                    "/health",
                    "/status"
                ).permitAll()
                
                // Archivos estáticos
                .requestMatchers(
                    "/css/**",
                    "/js/**", 
                    "/images/**",
                    "/img/**",
                    "/assets/**",
                    "/static/**",
                    "/public/**",
                    "/resources/**"
                ).permitAll()
                
                // Archivos subidos
                .requestMatchers("/uploads/**").permitAll()
                
                // API endpoints
                .requestMatchers("/api/**").permitAll()
                
                // Para desarrollo: también permitir /h2-console si usas H2
                .requestMatchers("/h2-console/**").permitAll()
                
                // CUALQUIER otra solicitud - ¡IMPORTANTE! Esto puede estar bloqueando
                .anyRequest().authenticated()  // Cambiado de permitAll() a authenticated()
            )
            
            // 4. Headers para H2 Console (si la usas)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Content-Disposition"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Método de WebMvcConfigurer - CORREGIDO
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. Archivos subidos
        String uploadPath = Paths.get("uploads").toAbsolutePath().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
        
        // 2. Archivos estáticos de Spring Boot (CRÍTICO)
        registry.addResourceHandler("/**")
                .addResourceLocations(
                    "classpath:/static/",
                    "classpath:/public/", 
                    "classpath:/resources/",
                    "classpath:/META-INF/resources/"
                );
        
        // 3. Para desarrollo web (si tienes carpeta webapp)
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        
        System.out.println("📁 Configurado acceso a archivos estáticos:");
        System.out.println("   - Uploads: " + uploadPath);
        System.out.println("   - Recursos estáticos: classpath:/static/");
    }
    
    // Agrega este método también para mejor manejo de recursos
    public void configure(org.springframework.web.servlet.config.annotation.WebMvcConfigurer configurer) {
        // Habilitar el manejo de recursos estáticos por defecto
    }
}