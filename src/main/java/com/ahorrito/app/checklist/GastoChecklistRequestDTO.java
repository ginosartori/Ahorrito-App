package com.ahorrito.app.checklist;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record GastoChecklistRequestDTO(
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    String nombre,

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    BigDecimal monto,

    @NotNull(message = "La fecha es obligatoria")
    LocalDate fecha,

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    String descripcion,

    boolean permanente,

    @NotNull(message = "La billetera es obligatoria")
    Long carteraId,

    @NotNull(message = "La categoría es obligatoria")
    Long categoriaId
) {}
