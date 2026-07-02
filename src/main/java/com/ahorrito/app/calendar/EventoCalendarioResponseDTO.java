package com.ahorrito.app.calendar;

import java.time.LocalDate;

public record EventoCalendarioResponseDTO(
    Long id,
    String titulo,
    String descripcion,
    LocalDate fecha,
    String tipo
) {}
