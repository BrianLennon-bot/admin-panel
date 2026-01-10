package com.tienda.admin.repository;

import com.tienda.admin.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    List<Producto> findAllByOrderByNombreAsc();
    
    List<Producto> findByCategoriaOrderByNombreAsc(String categoria);
    
    List<Producto> findByOfertaEspecialOrderByNombreAsc(Boolean ofertaEspecial);
    
    List<Producto> findByNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);
    
    @Query("SELECT DISTINCT p.categoria FROM Producto p WHERE p.categoria IS NOT NULL ORDER BY p.categoria")
    List<String> findDistinctCategorias();
    
    @Query("SELECT p FROM Producto p WHERE " +
            "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
            "(:categoria IS NULL OR p.categoria = :categoria) AND " +
            "(:ofertaEspecial IS NULL OR p.ofertaEspecial = :ofertaEspecial) " +
            "ORDER BY p.nombre ASC")
    List<Producto> buscarProductos(String nombre, String categoria, Boolean ofertaEspecial);
}