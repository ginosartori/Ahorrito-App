package com.ahorrito.app.auth;

import com.ahorrito.app.auth.CodigoRecuperacion;
import com.ahorrito.app.auth.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodigoRecuperacionRepository extends JpaRepository<CodigoRecuperacion, Long> {
    Optional<CodigoRecuperacion> findByUsuario(Usuario usuario);
}
