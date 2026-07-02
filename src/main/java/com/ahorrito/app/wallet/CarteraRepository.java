package com.ahorrito.app.wallet;

import com.ahorrito.app.wallet.Cartera;
import com.ahorrito.app.auth.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CarteraRepository extends JpaRepository<Cartera, Long> {

    List<Cartera> findAllByUsuarioOrderByOrdenAscIdAsc(Usuario usuario);

    Optional<Cartera> findByIdAndUsuario(Long id, Usuario usuario);

    @Query("SELECT MAX(c.orden) FROM Cartera c WHERE c.usuario = :usuario")
    Integer findMaxOrdenByUsuario(@Param("usuario") Usuario usuario);

    @Query("SELECT COUNT(c) FROM Cartera c WHERE c.id IN :ids AND c.usuario.username = :username")
    long countByIdInAndUsuarioUsername(@Param("ids") List<Long> ids, @Param("username") String username);
}
