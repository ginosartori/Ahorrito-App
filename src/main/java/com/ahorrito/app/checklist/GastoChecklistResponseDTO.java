package com.ahorrito.app.checklist;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GastoChecklistResponseDTO(
    Long id,
    String nombre,
    BigDecimal monto,
    LocalDate fecha,
    String descripcion,
    boolean permanente,
    boolean completado,
    Long carteraId,
    String carteraNombre,
    Long categoriaId,
    String categoriaNombre,
    String categoriaColor,
    Long transaccionId
) {}
