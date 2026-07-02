package com.ahorrito.app.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequestDTO(
    @NotBlank(message = "El nombre de la categoría no puede estar vacío")
    @Size(max = 100, message = "El nombre de la categoría no puede superar los 100 caracteres")
    String nombre,

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    String descripcion,

    @Size(max = 20, message = "El color no puede superar los 20 caracteres")
    String color,

    java.math.BigDecimal limiteGasto
) {}
