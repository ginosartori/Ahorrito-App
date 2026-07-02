package com.ahorrito.app.wallet;
import com.ahorrito.app.auth.SecurityService;

import com.ahorrito.app.wallet.CarteraRequestDTO;
import com.ahorrito.app.wallet.CarteraResponseDTO;
import com.ahorrito.app.wallet.Cartera;
import com.ahorrito.app.wallet.CarteraMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarteraServiceTest {

    @Mock
    private CarteraRepository carteraRepository;

    @Mock
    private TransaccionRepository transaccionRepository;

    @Mock
    private CarteraMapper carteraMapper;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private CarteraService carteraService;

    private Cartera cartera;
    private CarteraRequestDTO requestDTO;
    private CarteraResponseDTO responseDTO;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().id(1L).username("testuser").build();

        cartera = Cartera.builder()
                .id(1L)
                .nombre("Banco Galicia")
                .montoObjetivo(null)
                .montoActual(new BigDecimal("5000.00"))
                .descripcion("Cuenta sueldo")
                .esObjetivoAhorro(false)
                .usuario(usuario)
                .build();

        requestDTO = new CarteraRequestDTO("Banco Galicia", null, new BigDecimal("5000.00"), "Cuenta sueldo", false, "gold");
        responseDTO = new CarteraResponseDTO(1L, "Banco Galicia", null, new BigDecimal("5000.00"), "Cuenta sueldo", false, "gold");
    }

    @Test
    void findAll_ShouldReturnList() {
        when(securityService.getLoggedUser()).thenReturn(usuario);
        when(carteraRepository.findAllByUsuarioOrderByOrdenAscIdAsc(usuario)).thenReturn(List.of(cartera));
        when(carteraMapper.toResponseDTO(cartera)).thenReturn(responseDTO);

        List<CarteraResponseDTO> result = carteraService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Banco Galicia", result.get(0).nombre());
    }

    @Test
    void delete_ShouldDeleteCarteraAndAssociatedTransactions() {
        when(securityService.getLoggedUser()).thenReturn(usuario);
        when(carteraRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.of(cartera));

        assertDoesNotThrow(() -> carteraService.delete(1L));

        verify(transaccionRepository).deleteByCarteraId(1L);
        verify(carteraRepository).delete(cartera);
    }
}
