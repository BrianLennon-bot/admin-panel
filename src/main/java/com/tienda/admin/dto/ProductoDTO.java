package com.tienda.admin.dto;

import java.math.BigDecimal;

public class ProductoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String categoria;
    private Boolean ofertaEspecial = false;
    private String imagen;
    private String img;
    
    // Constructores
    public ProductoDTO() {}
    
    public ProductoDTO(Long id, String nombre, String descripcion, BigDecimal precio, 
                        String categoria, Boolean ofertaEspecial, String imagen) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.ofertaEspecial = ofertaEspecial;
        this.imagen = imagen;
        this.img = imagen;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public BigDecimal getPrecio() {
        return precio;
    }
    
    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public Boolean getOfertaEspecial() {
        return ofertaEspecial;
    }
    
    public void setOfertaEspecial(Boolean ofertaEspecial) {
        this.ofertaEspecial = ofertaEspecial;
    }
    
    public String getImagen() {
        return imagen;
    }
    
    public void setImagen(String imagen) {
        this.imagen = imagen;
        this.img = imagen;
    }
    
    public String getImg() {
        return img;
    }
    
    public void setImg(String img) {
        this.img = img;
        this.imagen = img;
    }
}