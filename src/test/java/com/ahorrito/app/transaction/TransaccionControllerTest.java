package com.ahorrito.app.transaction;

import tools.jackson.databind.ObjectMapper;
import com.ahorrito.app.transaction.TransaccionRequestDTO;
import com.ahorrito.app.transaction.TransaccionResponseDTO;
import com.ahorrito.app.transaction.TipoTransaccion;
import com.ahorrito.app.transaction.TransaccionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransaccionController.class)
@WithMockUser
class TransaccionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransaccionService transaccionService;

    private TransaccionResponseDTO responseDTO;
    private TransaccionRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new TransaccionResponseDTO(10L, "Sueldo", new BigDecimal("1500.00"), LocalDate.now(), TipoTransaccion.INGRESO, null, null);
        requestDTO = new TransaccionRequestDTO("Sueldo", new BigDecimal("1500.00"), LocalDate.now(), TipoTransaccion.INGRESO, null, 1L);
    }

    @Test
    void getAll_ShouldReturnList() throws Exception {
        when(transaccionService.findAll()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/transacciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].descripcion").value("Sueldo"));

        verify(transaccionService).findAll();
    }

    @Test
    void create_WithValidDTO_ShouldReturnCreated() throws Exception {
        when(transaccionService.create(any(TransaccionRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/transacciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.monto").value(1500.00));
    }
}
