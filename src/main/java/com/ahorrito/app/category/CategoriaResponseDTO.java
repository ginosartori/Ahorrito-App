package com.ahorrito.app.category;

public record CategoriaResponseDTO(
    Long id,
    String nombre,
    String descripcion,
    String color,
    java.math.BigDecimal limiteGasto
) {}
