package com.ahorrito.app.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecuperarPasswordDTO {

    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    private String username;

    @NotBlank(message = "El código de recuperación no puede estar vacío")
    private String recoveryCode;

    @NotBlank(message = "La nueva contraseña no puede estar vacía")
    @Size(min = 4, max = 100, message = "La nueva contraseña debe tener al menos 4 caracteres")
    private String newPassword;
}
