package com.ahorrito.app.category;

import com.ahorrito.app.category.Categoria;
import com.ahorrito.app.auth.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findAllByUsuario(Usuario usuario);
    Optional<Categoria> findByIdAndUsuario(Long id, Usuario usuario);
    boolean existsByNombreIgnoreCaseAndUsuario(String nombre, Usuario usuario);
}
