package com.ahorrito.app.calendar;
import com.ahorrito.app.auth.SecurityService;

import com.ahorrito.app.calendar.EventoCalendarioRequestDTO;
import com.ahorrito.app.calendar.EventoCalendarioResponseDTO;
import com.ahorrito.app.calendar.EventoCalendario;
import com.ahorrito.app.auth.Usuario;
import com.ahorrito.app.exception.ResourceNotFoundException;
import com.ahorrito.app.calendar.EventoCalendarioMapper;
import com.ahorrito.app.calendar.EventoCalendarioRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventoCalendarioService {

    private final EventoCalendarioRepository eventoRepository;
    private final EventoCalendarioMapper eventoMapper;
    private final SecurityService securityService;

    @Transactional(readOnly = true)
    public List<EventoCalendarioResponseDTO> findAll() {
        Usuario usuario = securityService.getLoggedUser();
        return eventoRepository.findAllByUsuario(usuario).stream()
                .map(eventoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@securityService.isEventoOwner(#id)")
    public EventoCalendarioResponseDTO findById(Long id) {
        Usuario usuario = securityService.getLoggedUser();
        EventoCalendario evento = eventoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + id));
        return eventoMapper.toResponseDTO(evento);
    }

    @Transactional
    public EventoCalendarioResponseDTO create(EventoCalendarioRequestDTO requestDTO) {
        Usuario usuario = securityService.getLoggedUser();
        EventoCalendario evento = eventoMapper.toEntity(requestDTO);
        evento.setUsuario(usuario);
        EventoCalendario saved = eventoRepository.save(evento);
        return eventoMapper.toResponseDTO(saved);
    }

    @Transactional
    @PreAuthorize("@securityService.isEventoOwner(#id)")
    public EventoCalendarioResponseDTO update(Long id, EventoCalendarioRequestDTO requestDTO) {
        Usuario usuario = securityService.getLoggedUser();
        EventoCalendario evento = eventoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + id));

        eventoMapper.updateEntityFromDTO(requestDTO, evento);
        return eventoMapper.toResponseDTO(evento);
    }

    @Transactional
    @PreAuthorize("@securityService.isEventoOwner(#id)")
    public void delete(Long id) {
        Usuario usuario = securityService.getLoggedUser();
        EventoCalendario evento = eventoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + id));
        eventoRepository.delete(evento);
    }
}
