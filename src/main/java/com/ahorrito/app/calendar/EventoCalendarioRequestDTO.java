package com.ahorrito.app.calendar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record EventoCalendarioRequestDTO(
    @NotBlank(message = "El título del evento es obligatorio")
    @Size(max = 100, message = "El título no puede superar los 100 caracteres")
    String titulo,

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    String descripcion,

    @NotNull(message = "La fecha del evento es obligatoria")
    LocalDate fecha,

    @NotBlank(message = "El tipo de evento es obligatorio")
    @Size(max = 50, message = "El tipo no puede superar los 50 caracteres")
    String tipo
) {}
