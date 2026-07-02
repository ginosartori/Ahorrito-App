package com.ahorrito.app.checklist;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checklist")
@RequiredArgsConstructor
public class GastoChecklistController {

    private final GastoChecklistService gastoChecklistService;

    @GetMapping
    public ResponseEntity<List<GastoChecklistResponseDTO>> getAll(
            @RequestParam(value = "anio", required = false) Integer anio,
            @RequestParam(value = "mes", required = false) Integer mes) {
        return ResponseEntity.ok(gastoChecklistService.findAllForMonth(anio, mes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GastoChecklistResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(gastoChecklistService.findById(id));
    }

    @PostMapping
    public ResponseEntity<GastoChecklistResponseDTO> create(@Valid @RequestBody GastoChecklistRequestDTO requestDTO) {
        return new ResponseEntity<>(gastoChecklistService.create(requestDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GastoChecklistResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody GastoChecklistRequestDTO requestDTO) {
        return ResponseEntity.ok(gastoChecklistService.update(id, requestDTO));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<GastoChecklistResponseDTO> toggleCompleted(@PathVariable Long id) {
        return ResponseEntity.ok(gastoChecklistService.toggleCompleted(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        gastoChecklistService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
