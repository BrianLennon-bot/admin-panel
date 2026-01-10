package com.tienda.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AdminPanelApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminPanelApplication.class, args);
        System.out.println("✅ Aplicación de administración iniciada en http://localhost:8080");
        System.out.println("✅ API disponible en http://localhost:8080/api/productos");
    }
}