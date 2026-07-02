package com.ahorrito.app.security;

import tools.jackson.databind.ObjectMapper;
import com.ahorrito.app.wallet.CarteraRequestDTO;
import com.ahorrito.app.transaction.TransaccionRequestDTO;
import com.ahorrito.app.wallet.Cartera;
import com.ahorrito.app.category.Categoria;
import com.ahorrito.app.calendar.EventoCalendario;
import com.ahorrito.app.transaction.TipoTransaccion;
import com.ahorrito.app.transaction.Transaccion;
import com.ahorrito.app.auth.Usuario;
import com.ahorrito.app.wallet.CarteraRepository;
import com.ahorrito.app.category.CategoriaRepository;
import com.ahorrito.app.auth.CodigoRecuperacionRepository;
import com.ahorrito.app.calendar.EventoCalendarioRepository;
import com.ahorrito.app.profile.PerfilRepository;
import com.ahorrito.app.transaction.TransaccionRepository;
import com.ahorrito.app.auth.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:securitytestdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=none",
    "app.security.seed-admin=false"
})
@AutoConfigureMockMvc
class SecurityDataIsolationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CarteraRepository carteraRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private EventoCalendarioRepository eventoRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private CodigoRecuperacionRepository codigoRecuperacionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario userA;
    private Usuario userB;

    private Cartera carteraUserA;
    private Categoria categoriaUserA;
    private Transaccion transaccionUserA;
    private EventoCalendario eventoUserA;

    @BeforeEach
    void setUp() {
        // Limpieza previa
        cleanupDatabase();

        // Crear y guardar usuarios
        userA = Usuario.builder()
                .username("userA")
                .password(passwordEncoder.encode("passwordA"))
                .build();
        userB = Usuario.builder()
                .username("userB")
                .password(passwordEncoder.encode("passwordB"))
                .build();
        usuarioRepository.save(userA);
        usuarioRepository.save(userB);

        // Crear recursos para userA
        carteraUserA = Cartera.builder()
                .nombre("Cartera de A")
                .montoObjetivo(new BigDecimal("1000.00"))
                .montoActual(new BigDecimal("500.00"))
                .descripcion("Descripción A")
                .esObjetivoAhorro(false)
                .color("blue")
                .orden(1)
                .usuario(userA)
                .build();
        carteraRepository.save(carteraUserA);

        categoriaUserA = Categoria.builder()
                .nombre("Comida")
                .descripcion("Gastos en comida")
                .color("red")
                .limiteGasto(new BigDecimal("200.00"))
                .usuario(userA)
                .build();
        categoriaRepository.save(categoriaUserA);

        transaccionUserA = Transaccion.builder()
                .descripcion("Almuerzo")
                .monto(new BigDecimal("15.50"))
                .fecha(LocalDate.now())
                .tipo(TipoTransaccion.GASTO)
                .cartera(carteraUserA)
                .categoria(categoriaUserA)
                .usuario(userA)
                .build();
        transaccionRepository.save(transaccionUserA);

        eventoUserA = EventoCalendario.builder()
                .titulo("Pagar Alquiler")
                .descripcion("Recordatorio mensual")
                .fecha(LocalDate.now().plusDays(5))
                .tipo("Alerta")
                .usuario(userA)
                .build();
        eventoRepository.save(eventoUserA);
    }

    @AfterEach
    void tearDown() {
        cleanupDatabase();
    }

    private void cleanupDatabase() {
        transaccionRepository.deleteAll();
        eventoRepository.deleteAll();
        categoriaRepository.deleteAll();
        carteraRepository.deleteAll();
        perfilRepository.deleteAll();
        codigoRecuperacionRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    // --- PRUEBAS DE AISLAMIENTO PARA CARTERAS ---

    @Test
    void getCartera_FromOtherUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/carteras/" + carteraUserA.getId()).with(user("userB")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCartera_FromOwnUser_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/carteras/" + carteraUserA.getId()).with(user("userA")))
                .andExpect(status().isOk());
    }

    @Test
    void updateCartera_FromOtherUser_ShouldReturnForbidden() throws Exception {
        CarteraRequestDTO updateRequest = new CarteraRequestDTO(
                "Nombre Hackeado",
                new BigDecimal("2000.00"),
                new BigDecimal("100.00"),
                "Hackeada",
                false,
                "yellow",
                1
        );

        mockMvc.perform(put("/api/carteras/" + carteraUserA.getId())
                        .with(user("userB"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteCartera_FromOtherUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/carteras/" + carteraUserA.getId()).with(user("userB")))
                .andExpect(status().isForbidden());
    }

    // --- PRUEBAS DE AISLAMIENTO PARA TRANSACCIONES ---

    @Test
    void getTransaccion_FromOtherUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/transacciones/" + transaccionUserA.getId()).with(user("userB")))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateTransaccion_FromOtherUser_ShouldReturnForbidden() throws Exception {
        TransaccionRequestDTO updateRequest = new TransaccionRequestDTO(
                "Hack",
                new BigDecimal("500.00"),
                LocalDate.now(),
                TipoTransaccion.INGRESO,
                null,
                carteraUserA.getId()
        );

        mockMvc.perform(put("/api/transacciones/" + transaccionUserA.getId())
                        .with(user("userB"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteTransaccion_FromOtherUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/transacciones/" + transaccionUserA.getId()).with(user("userB")))
                .andExpect(status().isForbidden());
    }

    @Test
    void createTransaccion_WithOtherUserCartera_ShouldReturnForbidden() throws Exception {
        // Intentar crear una transacción de userB usando la cartera de userA
        TransaccionRequestDTO createRequest = new TransaccionRequestDTO(
                "Intrusión",
                new BigDecimal("50.00"),
                LocalDate.now(),
                TipoTransaccion.GASTO,
                null,
                carteraUserA.getId()
        );

        mockMvc.perform(post("/api/transacciones")
                        .with(user("userB"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    // --- PRUEBAS DE AISLAMIENTO PARA CATEGORIAS ---

    @Test
    void getCategoria_FromOtherUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/categorias/" + categoriaUserA.getId()).with(user("userB")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteCategoria_FromOtherUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/categorias/" + categoriaUserA.getId()).with(user("userB")))
                .andExpect(status().isForbidden());
    }

    // --- PRUEBAS DE AISLAMIENTO PARA EVENTOS ---

    @Test
    void getEvento_FromOtherUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/eventos/" + eventoUserA.getId()).with(user("userB")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteEvento_FromOtherUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/eventos/" + eventoUserA.getId()).with(user("userB")))
                .andExpect(status().isForbidden());
    }
}
