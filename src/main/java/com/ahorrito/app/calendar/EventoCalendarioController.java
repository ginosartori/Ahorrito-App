package com.ahorrito.app.calendar;

import com.ahorrito.app.calendar.EventoCalendarioRequestDTO;
import com.ahorrito.app.calendar.EventoCalendarioResponseDTO;
import com.ahorrito.app.calendar.EventoCalendarioService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoCalendarioController {

    private final EventoCalendarioService eventoService;

    @GetMapping
    public ResponseEntity<List<EventoCalendarioResponseDTO>> getAll() {
        return ResponseEntity.ok(eventoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoCalendarioResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<EventoCalendarioResponseDTO> create(@Valid @RequestBody EventoCalendarioRequestDTO requestDTO) {
        return new ResponseEntity<>(eventoService.create(requestDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoCalendarioResponseDTO> update(@PathVariable Long id, @Valid @RequestBody EventoCalendarioRequestDTO requestDTO) {
        return ResponseEntity.ok(eventoService.update(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
