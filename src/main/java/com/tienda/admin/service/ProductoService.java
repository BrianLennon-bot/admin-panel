package com.tienda.admin.service;

import com.tienda.admin.dto.ProductoDTO;
import com.tienda.admin.model.Producto;
import com.tienda.admin.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {
    
    @Autowired
    private ProductoRepository productoRepository;
    
    public List<ProductoDTO> obtenerTodosLosProductos() {
        return productoRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    public ProductoDTO obtenerProductoPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        return convertirADTO(producto);
    }
    
    public ProductoDTO crearProducto(ProductoDTO productoDTO) {
        // Validaciones
        if (productoDTO.getNombre() == null || productoDTO.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre del producto es requerido");
        }
        
        if (productoDTO.getPrecio() == null || productoDTO.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El precio debe ser mayor a 0");
        }
        
        // Crear entidad
        Producto producto = new Producto();
        producto.setNombre(productoDTO.getNombre());
        producto.setDescripcion(productoDTO.getDescripcion());
        producto.setPrecio(productoDTO.getPrecio());
        producto.setCategoria(productoDTO.getCategoria());
        producto.setOfertaEspecial(productoDTO.getOfertaEspecial() != null ? productoDTO.getOfertaEspecial() : false);
        producto.setImagen(productoDTO.getImagen());
        
        Producto productoGuardado = productoRepository.save(producto);
        return convertirADTO(productoGuardado);
    }
    
    public ProductoDTO actualizarProducto(Long id, ProductoDTO productoDTO) {
        // Verificar que existe
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        
        // Validaciones
        if (productoDTO.getNombre() == null || productoDTO.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre del producto es requerido");
        }
        
        if (productoDTO.getPrecio() == null || productoDTO.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El precio debe ser mayor a 0");
        }
        
        // Actualizar
        productoExistente.setNombre(productoDTO.getNombre());
        productoExistente.setDescripcion(productoDTO.getDescripcion());
        productoExistente.setPrecio(productoDTO.getPrecio());
        productoExistente.setCategoria(productoDTO.getCategoria());
        productoExistente.setOfertaEspecial(productoDTO.getOfertaEspecial() != null ? productoDTO.getOfertaEspecial() : false);
        productoExistente.setImagen(productoDTO.getImagen());
        productoExistente.preUpdate();
        
        Producto productoActualizado = productoRepository.save(productoExistente);
        return convertirADTO(productoActualizado);
    }
    
    public void eliminarProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
        productoRepository.deleteById(id);
    }
    
    public List<ProductoDTO> buscarProductos(String nombre, String categoria, Boolean ofertaEspecial) {
        return productoRepository.buscarProductos(nombre, categoria, ofertaEspecial)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    public List<String> obtenerCategorias() {
        return productoRepository.findDistinctCategorias();
    }
    
    private ProductoDTO convertirADTO(Producto producto) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setCategoria(producto.getCategoria());
        dto.setOfertaEspecial(producto.getOfertaEspecial());
        dto.setImagen(producto.getImagen());
        return dto;
    }
}