package com.ahorrito.app.profile;

import com.ahorrito.app.profile.Perfil;
import com.ahorrito.app.auth.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    Optional<Perfil> findByUsuario(Usuario usuario);
}
