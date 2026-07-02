package com.ahorrito.app.calendar;

import com.ahorrito.app.calendar.EventoCalendario;
import com.ahorrito.app.auth.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoCalendarioRepository extends JpaRepository<EventoCalendario, Long> {
    List<EventoCalendario> findAllByUsuario(Usuario usuario);
    Optional<EventoCalendario> findByIdAndUsuario(Long id, Usuario usuario);
}
