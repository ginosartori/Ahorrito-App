package com.ahorrito.app.transaction;
import com.ahorrito.app.auth.SecurityService;

import com.ahorrito.app.wallet.CarteraResponseDTO;
import com.ahorrito.app.transaction.TransaccionRequestDTO;
import com.ahorrito.app.transaction.TransaccionResponseDTO;
import com.ahorrito.app.category.Categoria;
import com.ahorrito.app.wallet.Cartera;
import com.ahorrito.app.transaction.Transaccion;
import com.ahorrito.app.transaction.TipoTransaccion;
import com.ahorrito.app.exception.ResourceNotFoundException;
import com.ahorrito.app.transaction.TransaccionMapper;
import com.ahorrito.app.category.CategoriaRepository;
import com.ahorrito.app.wallet.CarteraRepository;
import com.ahorrito.app.transaction.TransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ahorrito.app.auth.Usuario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransaccionServiceTest {

    @Mock
    private TransaccionRepository transaccionRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private CarteraRepository carteraRepository;

    @Mock
    private TransaccionMapper transaccionMapper;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private TransaccionService transaccionService;

    private Cartera cartera;
    private Transaccion transaccion;
    private TransaccionRequestDTO requestDTO;
    private TransaccionResponseDTO responseDTO;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().id(1L).username("testuser").build();

        cartera = Cartera.builder()
                .id(1L)
                .nombre("Efectivo")
                .montoActual(new BigDecimal("1000.00"))
                .esObjetivoAhorro(false)
                .usuario(usuario)
                .build();

        transaccion = Transaccion.builder()
                .id(10L)
                .descripcion("Supermercado")
                .monto(new BigDecimal("200.00"))
                .fecha(LocalDate.now())
                .tipo(TipoTransaccion.GASTO)
                .cartera(cartera)
                .usuario(usuario)
                .build();

        requestDTO = new TransaccionRequestDTO("Supermercado", new BigDecimal("200.00"), LocalDate.now(), TipoTransaccion.GASTO, null, 1L);
        CarteraResponseDTO carteraResponse = new CarteraResponseDTO(1L, "Efectivo", null, new BigDecimal("800.00"), null, false, "gold");
        responseDTO = new TransaccionResponseDTO(10L, "Supermercado", new BigDecimal("200.00"), LocalDate.now(), TipoTransaccion.GASTO, null, carteraResponse);
    }

    @Test
    void create_Gasto_ShouldDecreaseWalletBalance() {
        when(securityService.getLoggedUser()).thenReturn(usuario);
        when(carteraRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.of(cartera));
        when(transaccionMapper.toEntity(requestDTO)).thenReturn(transaccion);
        when(transaccionRepository.save(transaccion)).thenReturn(transaccion);
        when(transaccionMapper.toResponseDTO(transaccion)).thenReturn(responseDTO);

        TransaccionResponseDTO result = transaccionService.create(requestDTO);

        assertNotNull(result);
        assertEquals(new BigDecimal("800.00"), cartera.getMontoActual());
        verify(carteraRepository).findByIdAndUsuario(1L, usuario);
        verify(transaccionRepository).save(transaccion);
    }

    @Test
    void create_Ingreso_ShouldIncreaseWalletBalance() {
        TransaccionRequestDTO ingresoRequest = new TransaccionRequestDTO("Sueldo", new BigDecimal("500.00"), LocalDate.now(), TipoTransaccion.INGRESO, null, 1L);
        transaccion.setTipo(TipoTransaccion.INGRESO);
        transaccion.setMonto(new BigDecimal("500.00"));

        when(securityService.getLoggedUser()).thenReturn(usuario);
        when(carteraRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.of(cartera));
        when(transaccionMapper.toEntity(ingresoRequest)).thenReturn(transaccion);
        when(transaccionRepository.save(transaccion)).thenReturn(transaccion);
        when(transaccionMapper.toResponseDTO(transaccion)).thenReturn(responseDTO);

        transaccionService.create(ingresoRequest);

        assertEquals(new BigDecimal("1500.00"), cartera.getMontoActual());
    }

    @Test
    void delete_ShouldReverseBalanceEffect() {
        when(securityService.getLoggedUser()).thenReturn(usuario);
        when(transaccionRepository.findByIdAndUsuario(10L, usuario)).thenReturn(Optional.of(transaccion));

        transaccionService.delete(10L);

        // Reverse GASTO of 200.00 from 1000.00 should restore to 1200.00
        assertEquals(new BigDecimal("1200.00"), cartera.getMontoActual());
        verify(transaccionRepository).delete(transaccion);
    }
}
