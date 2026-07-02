package com.ahorrito.app.auth;

import com.ahorrito.app.auth.RecuperarPasswordDTO;
import com.ahorrito.app.auth.RegistroRequestDTO;
import com.ahorrito.app.auth.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    @PostMapping("/register")
    public ResponseEntity<?> registrar(@Valid @RequestBody RegistroRequestDTO registroDTO) {
        try {
            UsuarioService.RegistroResult result = usuarioService.registrar(registroDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Usuario registrado exitosamente",
                    "username", result.usuario().getUsername(),
                    "recoveryCode", result.recoveryCode()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/recover")
    public ResponseEntity<?> recuperarPassword(@Valid @RequestBody RecuperarPasswordDTO recuperarDTO) {
        try {
            String nuevoCodigoRecuperacion = usuarioService.recuperarPassword(recuperarDTO);
            return ResponseEntity.ok(Map.of(
                    "message", "Contraseña actualizada exitosamente",
                    "recoveryCode", nuevoCodigoRecuperacion
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getSesion(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No autenticado"));
        }
        return ResponseEntity.ok(Map.of("username", principal.getName()));
    }
}
