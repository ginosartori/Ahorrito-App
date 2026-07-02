package com.ahorrito.app.transaction;

import com.ahorrito.app.transaction.TransaccionRequestDTO;
import com.ahorrito.app.transaction.TransaccionResponseDTO;
import com.ahorrito.app.transaction.TransaccionService;
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
@RequestMapping("/api/transacciones")
@RequiredArgsConstructor
public class TransaccionController {

    private final TransaccionService transaccionService;

    @GetMapping
    public ResponseEntity<List<TransaccionResponseDTO>> getAll() {
        return ResponseEntity.ok(transaccionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransaccionResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transaccionService.findById(id));
    }

    @PostMapping
    public ResponseEntity<TransaccionResponseDTO> create(@Valid @RequestBody TransaccionRequestDTO requestDTO) {
        return new ResponseEntity<>(transaccionService.create(requestDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransaccionResponseDTO> update(@PathVariable Long id, @Valid @RequestBody TransaccionRequestDTO requestDTO) {
        return ResponseEntity.ok(transaccionService.update(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transaccionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
