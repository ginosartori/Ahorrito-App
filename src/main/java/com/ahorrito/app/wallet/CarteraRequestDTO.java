package com.ahorrito.app.wallet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CarteraRequestDTO(
    @NotBlank(message = "El nombre de la cartera no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    String nombre,

    @DecimalMin(value = "0.00", message = "El monto objetivo debe ser mayor o igual a cero")
    BigDecimal montoObjetivo,

    @NotNull(message = "El monto actual es obligatorio")
    @DecimalMin(value = "-999999999.99", message = "El balance ingresado no es válido")
    BigDecimal montoActual,

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    String descripcion,

    boolean esObjetivoAhorro,

    @Size(max = 20, message = "El color no puede superar los 20 caracteres")
    String color,

    Integer orden
) {
    public CarteraRequestDTO(String nombre, BigDecimal montoObjetivo, BigDecimal montoActual, String descripcion, boolean esObjetivoAhorro, String color) {
        this(nombre, montoObjetivo, montoActual, descripcion, esObjetivoAhorro, color, 0);
    }
}
