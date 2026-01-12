package com.tienda.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home() {
        return "forward:/index.html";
    }
    
    // Esto manejará todas las rutas de tu SPA (Single Page Application)
    @GetMapping("/{path:[^\\.]*}")
    public String redirectToIndex() {
        return "forward:/index.html";
    }

    // Endpoint de salud para Render
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}