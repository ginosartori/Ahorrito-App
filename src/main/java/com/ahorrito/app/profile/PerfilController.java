package com.ahorrito.app.profile;

import com.ahorrito.app.profile.PerfilDTO;
import com.ahorrito.app.profile.PerfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfilService;

    @GetMapping
    public ResponseEntity<PerfilDTO> getPerfil() {
        return ResponseEntity.ok(perfilService.getPerfil());
    }

    @PutMapping
    public ResponseEntity<PerfilDTO> updatePerfil(@Valid @RequestBody PerfilDTO perfilDTO) {
        return ResponseEntity.ok(perfilService.updatePerfil(perfilDTO));
    }
}
