package com.ahorrito.app.checklist;

import com.ahorrito.app.auth.Usuario;
import com.ahorrito.app.category.Categoria;
import com.ahorrito.app.wallet.Cartera;
import com.ahorrito.app.transaction.Transaccion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "gasto_checklist",
    indexes = {
        @Index(name = "idx_gasto_checklist_usuario_fecha", columnList = "usuario_id, fecha"),
        @Index(name = "idx_gasto_checklist_cartera_id", columnList = "cartera_id"),
        @Index(name = "idx_gasto_checklist_categoria_id", columnList = "categoria_id"),
        @Index(name = "idx_gasto_checklist_transaccion_id", columnList = "transaccion_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GastoChecklist implements com.ahorrito.app.auth.UserOwnedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false)
    private boolean permanente;

    @Column(nullable = false)
    private boolean completado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartera_id", nullable = false)
    private Cartera cartera;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaccion_id")
    private Transaccion transaccion;
}
