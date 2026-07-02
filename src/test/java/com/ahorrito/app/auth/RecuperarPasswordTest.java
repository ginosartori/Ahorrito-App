package com.ahorrito.app.auth;

import com.ahorrito.app.auth.RecuperarPasswordDTO;
import com.ahorrito.app.auth.RegistroRequestDTO;
import com.ahorrito.app.auth.CodigoRecuperacion;
import com.ahorrito.app.auth.Usuario;
import com.ahorrito.app.auth.CodigoRecuperacionRepository;
import com.ahorrito.app.auth.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecuperarPasswordTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CodigoRecuperacionRepository codigoRecuperacionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        // Configurar los @Value fields que Mockito no inyecta
        ReflectionTestUtils.setField(usuarioService, "registrationEnabled", true);
        ReflectionTestUtils.setField(usuarioService, "maxUsers", 100);

        usuario = Usuario.builder()
                .id(1L)
                .username("testuser")
                .password("hashedOldPassword")
                .build();
    }

    // --- Tests de Registro con Código ---

    @Test
    void registrar_ShouldReturnRecoveryCode() {
        RegistroRequestDTO dto = RegistroRequestDTO.builder()
                .username("newuser")
                .password("pass1234")
                .build();

        when(usuarioRepository.count()).thenReturn(0L);
        when(usuarioRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(passwordEncoder.encode(anyString())).thenReturn("encodedValue");
        when(codigoRecuperacionRepository.findByUsuario(any())).thenReturn(Optional.empty());
        when(codigoRecuperacionRepository.save(any(CodigoRecuperacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UsuarioService.RegistroResult result = usuarioService.registrar(dto);

        assertNotNull(result);
        assertNotNull(result.usuario());
        assertNotNull(result.recoveryCode());
        assertTrue(result.recoveryCode().startsWith("AHORR-"),
                "El código debe comenzar con AHORR-");
        assertEquals("newuser", result.usuario().getUsername());

        // Verificar que se guardó el código de recuperación
        verify(codigoRecuperacionRepository).save(any(CodigoRecuperacion.class));
    }

    @Test
    void registrar_RecoveryCodeFormat_ShouldBeValid() {
        RegistroRequestDTO dto = RegistroRequestDTO.builder()
                .username("formatuser")
                .password("pass1234")
                .build();

        when(usuarioRepository.count()).thenReturn(0L);
        when(usuarioRepository.findByUsername("formatuser")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(3L);
            return u;
        });
        when(passwordEncoder.encode(anyString())).thenReturn("encodedValue");
        when(codigoRecuperacionRepository.findByUsuario(any())).thenReturn(Optional.empty());
        when(codigoRecuperacionRepository.save(any(CodigoRecuperacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UsuarioService.RegistroResult result = usuarioService.registrar(dto);
        String code = result.recoveryCode();

        // Formato: AHORR-XXXX-XXXX
        assertTrue(code.matches("AHORR-[A-Z0-9]{4}-[A-Z0-9]{4}"),
                "El código debe tener formato AHORR-XXXX-XXXX, pero fue: " + code);
    }

    // --- Tests de Recuperación ---

    @Test
    void recuperarPassword_WithValidCode_ShouldUpdatePasswordAndReturnNewCode() {
        String codigoOriginal = "AHORR-AB12-CD34";
        CodigoRecuperacion codigoRecuperacion = CodigoRecuperacion.builder()
                .id(1L)
                .codigoHash("hashedRecoveryCode")
                .usuario(usuario)
                .creadoEn(LocalDateTime.now())
                .build();

        RecuperarPasswordDTO dto = RecuperarPasswordDTO.builder()
                .username("testuser")
                .recoveryCode(codigoOriginal)
                .newPassword("newPass456")
                .build();

        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(usuario));
        when(codigoRecuperacionRepository.findByUsuario(usuario)).thenReturn(Optional.of(codigoRecuperacion));
        when(passwordEncoder.matches(codigoOriginal, "hashedRecoveryCode")).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedValue");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(codigoRecuperacionRepository.save(any(CodigoRecuperacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        String nuevoCodigoPlano = usuarioService.recuperarPassword(dto);

        // Se debe devolver un nuevo código
        assertNotNull(nuevoCodigoPlano);
        assertTrue(nuevoCodigoPlano.startsWith("AHORR-"));

        // Se debe actualizar la contraseña del usuario
        verify(usuarioRepository).save(usuario);
        assertEquals("newEncodedValue", usuario.getPassword());

        // Se debe actualizar el hash del código de recuperación
        ArgumentCaptor<CodigoRecuperacion> captor = ArgumentCaptor.forClass(CodigoRecuperacion.class);
        verify(codigoRecuperacionRepository).save(captor.capture());
        assertEquals("newEncodedValue", captor.getValue().getCodigoHash());
    }

    @Test
    void recuperarPassword_WithInvalidCode_ShouldThrowException() {
        CodigoRecuperacion codigoRecuperacion = CodigoRecuperacion.builder()
                .id(1L)
                .codigoHash("hashedRecoveryCode")
                .usuario(usuario)
                .creadoEn(LocalDateTime.now())
                .build();

        RecuperarPasswordDTO dto = RecuperarPasswordDTO.builder()
                .username("testuser")
                .recoveryCode("AHORR-XXXX-WRONG")
                .newPassword("newPass456")
                .build();

        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(usuario));
        when(codigoRecuperacionRepository.findByUsuario(usuario)).thenReturn(Optional.of(codigoRecuperacion));
        when(passwordEncoder.matches("AHORR-XXXX-WRONG", "hashedRecoveryCode")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.recuperarPassword(dto));

        assertEquals("Credenciales de recuperación inválidas", exception.getMessage());

        // La contraseña NO debe cambiar
        assertEquals("hashedOldPassword", usuario.getPassword());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void recuperarPassword_WithNonExistentUser_ShouldThrowException() {
        RecuperarPasswordDTO dto = RecuperarPasswordDTO.builder()
                .username("noexiste")
                .recoveryCode("AHORR-AB12-CD34")
                .newPassword("newPass456")
                .build();

        when(usuarioRepository.findByUsername("noexiste")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.recuperarPassword(dto));

        // Mensaje genérico para no revelar si el usuario existe
        assertEquals("Credenciales de recuperación inválidas", exception.getMessage());
    }

    @Test
    void recuperarPassword_OldCodeShouldNotWorkAfterRecovery() {
        String codigoOriginal = "AHORR-AB12-CD34";
        CodigoRecuperacion codigoRecuperacion = CodigoRecuperacion.builder()
                .id(1L)
                .codigoHash("hashedOriginalCode")
                .usuario(usuario)
                .creadoEn(LocalDateTime.now())
                .build();

        RecuperarPasswordDTO dto = RecuperarPasswordDTO.builder()
                .username("testuser")
                .recoveryCode(codigoOriginal)
                .newPassword("newPass456")
                .build();

        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(usuario));
        when(codigoRecuperacionRepository.findByUsuario(usuario)).thenReturn(Optional.of(codigoRecuperacion));
        when(passwordEncoder.matches(codigoOriginal, "hashedOriginalCode")).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("newHashedCode");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(codigoRecuperacionRepository.save(any(CodigoRecuperacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Primera recuperación: debe funcionar
        usuarioService.recuperarPassword(dto);

        // Verificar que el hash del código fue actualizado (rotado)
        assertNotEquals("hashedOriginalCode", codigoRecuperacion.getCodigoHash(),
                "El hash del código debe cambiar después de la recuperación");
    }

    // --- Test del generador de código ---

    @Test
    void generarCodigoRecuperacion_ShouldProduceUniqueCodes() {
        String code1 = usuarioService.generarCodigoRecuperacion();
        String code2 = usuarioService.generarCodigoRecuperacion();

        assertNotEquals(code1, code2, "Dos códigos generados deben ser diferentes");
        assertTrue(code1.startsWith("AHORR-"));
        assertTrue(code2.startsWith("AHORR-"));
    }
}
