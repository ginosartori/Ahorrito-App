package com.ahorrito.app.checklist;

import com.ahorrito.app.auth.Usuario;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GastoChecklistRepository extends JpaRepository<GastoChecklist, Long> {

    List<GastoChecklist> findAllByUsuario(Usuario usuario);

    List<GastoChecklist> findAllByUsuarioAndFechaBetween(Usuario usuario, LocalDate start, LocalDate end);

    Optional<GastoChecklist> findByIdAndUsuario(Long id, Usuario usuario);

    @Modifying
    @Query("UPDATE GastoChecklist g SET g.completado = false, g.transaccion = null WHERE g.permanente = true")
    void resetPermanentes();

    @Modifying
    @Query("DELETE FROM GastoChecklist g WHERE g.permanente = false AND g.fecha < :startOfMonth")
    void deleteNoPermanentesAnteriores(@Param("startOfMonth") LocalDate startOfMonth);
}
