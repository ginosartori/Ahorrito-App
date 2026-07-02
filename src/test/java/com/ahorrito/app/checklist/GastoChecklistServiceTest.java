package com.ahorrito.app.checklist;

import com.ahorrito.app.auth.SecurityService;
import com.ahorrito.app.auth.Usuario;
import com.ahorrito.app.category.Categoria;
import com.ahorrito.app.category.CategoriaRepository;
import com.ahorrito.app.transaction.Transaccion;
import com.ahorrito.app.transaction.TransaccionRepository;
import com.ahorrito.app.wallet.Cartera;
import com.ahorrito.app.wallet.CarteraRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GastoChecklistServiceTest {

    @Mock
    private GastoChecklistRepository gastoChecklistRepository;

    @Mock
    private CarteraRepository carteraRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private TransaccionRepository transaccionRepository;

    @Mock
    private GastoChecklistMapper gastoChecklistMapper;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private GastoChecklistService gastoChecklistService;

    private Usuario usuario;
    private Cartera cartera;
    private Categoria categoria;
    private GastoChecklist gasto;
    private GastoChecklistRequestDTO requestDTO;
    private GastoChecklistResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().id(1L).username("testuser").build();
        cartera = Cartera.builder().id(10L).nombre("Billetera").montoActual(new BigDecimal("1000.00")).esObjetivoAhorro(false).build();
        categoria = Categoria.builder().id(20L).nombre("Servicios").build();

        gasto = GastoChecklist.builder()
                .id(1L)
                .nombre("Luz")
                .monto(new BigDecimal("150.00"))
                .fecha(LocalDate.now())
                .permanente(true)
                .completado(false)
                .usuario(usuario)
                .cartera(cartera)
                .categoria(categoria)
                .build();

        requestDTO = new GastoChecklistRequestDTO("Luz", new BigDecimal("150.00"), LocalDate.now(), "Luz casa", true, 10L, 20L);
        responseDTO = new GastoChecklistResponseDTO(1L, "Luz", new BigDecimal("150.00"), LocalDate.now(), "Luz casa", true, false, 10L, "Billetera", 20L, "Servicios", "gold", null);
    }

    @Test
    void create_ShouldSaveAndReturnResponse() {
        when(securityService.getLoggedUser()).thenReturn(usuario);
        when(carteraRepository.findByIdAndUsuario(10L, usuario)).thenReturn(Optional.of(cartera));
        when(categoriaRepository.findByIdAndUsuario(20L, usuario)).thenReturn(Optional.of(categoria));
        when(gastoChecklistMapper.toEntity(requestDTO)).thenReturn(gasto);
        when(gastoChecklistRepository.save(gasto)).thenReturn(gasto);
        when(gastoChecklistMapper.toResponseDTO(gasto)).thenReturn(responseDTO);

        GastoChecklistResponseDTO result = gastoChecklistService.create(requestDTO);

        assertNotNull(result);
        assertEquals("Luz", result.nombre());
        verify(gastoChecklistRepository).save(gasto);
    }

    @Test
    void toggleCompleted_WhenNotCompleted_ShouldCreateTransactionAndDeductBalance() {
        when(securityService.getLoggedUser()).thenReturn(usuario);
        when(gastoChecklistRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.of(gasto));
        
        Transaccion mockTrans = Transaccion.builder().id(100L).monto(new BigDecimal("150.00")).cartera(cartera).build();
        when(transaccionRepository.save(any(Transaccion.class))).thenReturn(mockTrans);
        when(gastoChecklistRepository.save(gasto)).thenReturn(gasto);
        
        gastoChecklistService.toggleCompleted(1L);

        assertTrue(gasto.isCompletado());
        assertEquals(new BigDecimal("850.00"), cartera.getMontoActual()); // 1000 - 150
        verify(transaccionRepository).save(any(Transaccion.class));
        verify(carteraRepository).save(cartera);
    }

    @Test
    void toggleCompleted_WhenCompleted_ShouldDeleteTransactionAndRefundBalance() {
        gasto.setCompletado(true);
        Transaccion linkedTrans = Transaccion.builder().id(100L).monto(new BigDecimal("150.00")).cartera(cartera).build();
        gasto.setTransaccion(linkedTrans);

        when(securityService.getLoggedUser()).thenReturn(usuario);
        when(gastoChecklistRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.of(gasto));
        when(gastoChecklistRepository.save(gasto)).thenReturn(gasto);

        gastoChecklistService.toggleCompleted(1L);

        assertFalse(gasto.isCompletado());
        assertNull(gasto.getTransaccion());
        assertEquals(new BigDecimal("1150.00"), cartera.getMontoActual()); // 1000 + 150
        verify(transaccionRepository).delete(linkedTrans);
        verify(carteraRepository).save(cartera);
    }

    @Test
    void ejecutarReseteoMensual_ShouldCallRepoMethods() {
        gastoChecklistService.ejecutarReseteoMensual();

        verify(gastoChecklistRepository).resetPermanentes();
        verify(gastoChecklistRepository).deleteNoPermanentesAnteriores(any(LocalDate.class));
    }
}
