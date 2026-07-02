package com.ahorrito.app.wallet;

import java.math.BigDecimal;

public record CarteraResponseDTO(
    Long id,
    String nombre,
    BigDecimal montoObjetivo,
    BigDecimal montoActual,
    String descripcion,
    boolean esObjetivoAhorro,
    String color,
    Integer orden
) {
    public CarteraResponseDTO(Long id, String nombre, BigDecimal montoObjetivo, BigDecimal montoActual, String descripcion, boolean esObjetivoAhorro, String color) {
        this(id, nombre, montoObjetivo, montoActual, descripcion, esObjetivoAhorro, color, 0);
    }
}
