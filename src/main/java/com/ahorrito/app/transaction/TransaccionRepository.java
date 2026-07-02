package com.ahorrito.app.transaction;

import com.ahorrito.app.transaction.Transaccion;
import com.ahorrito.app.auth.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    @EntityGraph(attributePaths = {"categoria", "cartera"})
    List<Transaccion> findAllByUsuario(Usuario usuario);

    @EntityGraph(attributePaths = {"categoria", "cartera"})
    Optional<Transaccion> findByIdAndUsuario(Long id, Usuario usuario);

    List<Transaccion> findByCarteraId(Long carteraId);

    @Modifying
    @Query("DELETE FROM Transaccion t WHERE t.cartera.id = :carteraId")
    void deleteByCarteraId(@Param("carteraId") Long carteraId);
}
