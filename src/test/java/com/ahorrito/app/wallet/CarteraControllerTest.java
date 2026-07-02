package com.ahorrito.app.wallet;

import tools.jackson.databind.ObjectMapper;
import com.ahorrito.app.wallet.CarteraRequestDTO;
import com.ahorrito.app.wallet.CarteraResponseDTO;
import com.ahorrito.app.wallet.CarteraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarteraController.class)
@WithMockUser
class CarteraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CarteraService carteraService;

    private CarteraResponseDTO responseDTO;
    private CarteraRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new CarteraResponseDTO(1L, "Efectivo", null, new BigDecimal("1000.00"), null, false, "gold");
        requestDTO = new CarteraRequestDTO("Efectivo", null, new BigDecimal("1000.00"), null, false, "gold");
    }

    @Test
    void getAll_ShouldReturnList() throws Exception {
        when(carteraService.findAll()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/carteras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Efectivo"));

        verify(carteraService).findAll();
    }

    @Test
    void create_WithValidDTO_ShouldReturnCreated() throws Exception {
        when(carteraService.create(any(CarteraRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/carteras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Efectivo"));
    }
}
