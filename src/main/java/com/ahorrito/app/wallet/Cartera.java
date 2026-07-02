package com.ahorrito.app.wallet;
import com.ahorrito.app.auth.Usuario;

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
import jakarta.persistence.Version;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "carteras",
    indexes = {
        @Index(name = "idx_carteras_usuario_id", columnList = "usuario_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cartera implements com.ahorrito.app.auth.UserOwnedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(precision = 19, scale = 2)
    private BigDecimal montoObjetivo;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal montoActual;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false)
    private boolean esObjetivoAhorro;

    @Column(length = 20)
    private String color;

    @Column(name = "orden")
    private Integer orden;

    @Version
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

}
