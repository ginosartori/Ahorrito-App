package com.ahorrito.app.wallet;

import com.ahorrito.app.wallet.CarteraRequestDTO;
import com.ahorrito.app.wallet.CarteraResponseDTO;
import com.ahorrito.app.wallet.CarteraService;
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
@RequestMapping("/api/carteras")
@RequiredArgsConstructor
public class CarteraController {

    private final CarteraService carteraService;

    @GetMapping
    public ResponseEntity<List<CarteraResponseDTO>> getAll() {
        return ResponseEntity.ok(carteraService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarteraResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(carteraService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CarteraResponseDTO> create(@Valid @RequestBody CarteraRequestDTO requestDTO) {
        return new ResponseEntity<>(carteraService.create(requestDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarteraResponseDTO> update(@PathVariable Long id, @Valid @RequestBody CarteraRequestDTO requestDTO) {
        return ResponseEntity.ok(carteraService.update(id, requestDTO));
    }

    @PutMapping("/reorder")
    public ResponseEntity<Void> reorder(@RequestBody List<Long> ids) {
        carteraService.reorder(ids);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        carteraService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
