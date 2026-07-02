package com.ahorrito.app.calendar;

import tools.jackson.databind.ObjectMapper;
import com.ahorrito.app.calendar.EventoCalendarioRequestDTO;
import com.ahorrito.app.calendar.EventoCalendarioResponseDTO;
import com.ahorrito.app.calendar.EventoCalendarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventoCalendarioController.class)
@WithMockUser
class EventoCalendarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventoCalendarioService service;

    private EventoCalendarioResponseDTO responseDTO;
    private EventoCalendarioRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new EventoCalendarioResponseDTO(1L, "Recordatorio Ahorro", "Mensual", LocalDate.now(), "RECORDATORIO");
        requestDTO = new EventoCalendarioRequestDTO("Recordatorio Ahorro", "Mensual", LocalDate.now(), "RECORDATORIO");
    }

    @Test
    void getAll_ShouldReturnList() throws Exception {
        when(service.findAll()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Recordatorio Ahorro"));

        verify(service).findAll();
    }

    @Test
    void create_WithValidDTO_ShouldReturnCreated() throws Exception {
        when(service.create(any(EventoCalendarioRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Recordatorio Ahorro"));
    }
}
