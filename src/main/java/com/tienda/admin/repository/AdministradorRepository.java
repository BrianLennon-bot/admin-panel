package com.tienda.admin.repository;

import com.tienda.admin.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    
    // Buscar administrador por username y que esté activo
    Optional<Administrador> findByUsernameAndActivoTrue(String username);
    
    // Buscar por username (sin importar si está activo)
    Optional<Administrador> findByUsername(String username);
    
    // Verificar si existe un administrador con ese username
    boolean existsByUsername(String username);
    
    // Buscar por email
    Optional<Administrador> findByEmail(String email);
}
