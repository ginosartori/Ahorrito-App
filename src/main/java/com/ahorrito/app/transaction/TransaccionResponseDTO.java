package com.ahorrito.app.transaction;
import com.ahorrito.app.category.CategoriaResponseDTO;
import com.ahorrito.app.wallet.CarteraResponseDTO;

import com.ahorrito.app.transaction.TipoTransaccion;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransaccionResponseDTO(
    Long id,
    String descripcion,
    BigDecimal monto,
    LocalDate fecha,
    TipoTransaccion tipo,
    CategoriaResponseDTO categoria,
    CarteraResponseDTO cartera
) {}
