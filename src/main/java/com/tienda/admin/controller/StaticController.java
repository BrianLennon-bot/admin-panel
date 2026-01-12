package com.tienda.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaticController {
    
    // Ruta raíz - sirve index.html
    @GetMapping("/")
    public String serveIndex() {
        return "forward:/index.html";
    }
    
    // Rutas del SPA
    @GetMapping({"/login", "/dashboard", "/productos", "/admin"})
    public String serveSpaRoutes() {
        return "forward:/index.html";
    }
    
    // Catch-all para rutas del frontend
    @GetMapping("/{path:[^\\.]*}")
    public String catchAllFrontend() {
        return "forward:/index.html";
    }
}