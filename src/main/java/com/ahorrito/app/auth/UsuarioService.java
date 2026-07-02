package com.ahorrito.app.auth;

import com.ahorrito.app.auth.RecuperarPasswordDTO;
import com.ahorrito.app.auth.RegistroRequestDTO;
import com.ahorrito.app.auth.CodigoRecuperacion;
import com.ahorrito.app.auth.Usuario;
import com.ahorrito.app.auth.CodigoRecuperacionRepository;
import com.ahorrito.app.auth.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final CodigoRecuperacionRepository codigoRecuperacionRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String CODE_PREFIX = "AHORR";
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_SEGMENT_LENGTH = 4;
    private static final int CODE_SEGMENTS = 2;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${app.security.seed-admin:true}")
    private boolean seedAdmin;

    @Value("${app.security.registration-enabled:true}")
    private boolean registrationEnabled;

    @Value("${app.security.max-users:100}")
    private int maxUsers;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          CodigoRecuperacionRepository codigoRecuperacionRepository,
                          @Lazy PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.codigoRecuperacionRepository = codigoRecuperacionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        
        return new CustomUserDetails(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getPassword(),
                Collections.emptyList()
        );
    }

    @Transactional
    public RegistroResult registrar(RegistroRequestDTO dto) {
        if (!registrationEnabled) {
            throw new IllegalArgumentException("El registro de nuevos usuarios está deshabilitado");
        }

        if (usuarioRepository.count() >= maxUsers) {
            throw new IllegalArgumentException("El registro de nuevos usuarios ha alcanzado el límite permitido");
        }

        if (usuarioRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya está registrado");
        }

        Usuario usuario = Usuario.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();

        Usuario saved = usuarioRepository.save(usuario);

        // Generar código de recuperación
        String codigoPlano = generarCodigoRecuperacion();
        guardarCodigoRecuperacion(saved, codigoPlano);

        return new RegistroResult(saved, codigoPlano);
    }

    @Transactional
    public String recuperarPassword(RecuperarPasswordDTO dto) {
        Usuario usuario = usuarioRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales de recuperación inválidas"));

        CodigoRecuperacion codigoRecuperacion = codigoRecuperacionRepository.findByUsuario(usuario)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales de recuperación inválidas"));

        // Verificar el código de recuperación
        if (!passwordEncoder.matches(dto.getRecoveryCode(), codigoRecuperacion.getCodigoHash())) {
            throw new IllegalArgumentException("Credenciales de recuperación inválidas");
        }

        // Actualizar la contraseña
        usuario.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        usuarioRepository.save(usuario);

        // Regenerar código de recuperación (el viejo queda invalidado)
        String nuevoCodigoPlano = generarCodigoRecuperacion();
        codigoRecuperacion.setCodigoHash(passwordEncoder.encode(nuevoCodigoPlano));
        codigoRecuperacion.setCreadoEn(LocalDateTime.now());
        codigoRecuperacionRepository.save(codigoRecuperacion);

        return nuevoCodigoPlano;
    }

    @PostConstruct
    @Transactional
    public void seedAdminUser() {
        if (seedAdmin && usuarioRepository.count() == 0) {
            Usuario admin = Usuario.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .build();
            Usuario saved = usuarioRepository.save(admin);

            String codigoPlano = generarCodigoRecuperacion();
            guardarCodigoRecuperacion(saved, codigoPlano);

            System.out.println("====== USUARIO ADMINISTRADOR CREADO POR DEFECTO: admin / admin123 ======");
            System.out.println("====== CÓDIGO DE RECUPERACIÓN ADMIN: " + codigoPlano + " ======");
        }
    }

    /**
     * Genera un código de recuperación con formato AHORR-XXXX-XXXX
     * usando caracteres alfanuméricos sin ambigüedades (sin 0, O, I, 1, L).
     */
    String generarCodigoRecuperacion() {
        StringBuilder code = new StringBuilder(CODE_PREFIX);
        for (int s = 0; s < CODE_SEGMENTS; s++) {
            code.append('-');
            for (int i = 0; i < CODE_SEGMENT_LENGTH; i++) {
                code.append(CODE_CHARS.charAt(SECURE_RANDOM.nextInt(CODE_CHARS.length())));
            }
        }
        return code.toString();
    }

    private void guardarCodigoRecuperacion(Usuario usuario, String codigoPlano) {
        // Buscar si ya existe un código para este usuario
        CodigoRecuperacion codigo = codigoRecuperacionRepository.findByUsuario(usuario)
                .orElse(CodigoRecuperacion.builder()
                        .usuario(usuario)
                        .build());

        codigo.setCodigoHash(passwordEncoder.encode(codigoPlano));
        codigo.setCreadoEn(LocalDateTime.now());
        codigoRecuperacionRepository.save(codigo);
    }

    /**
     * Resultado del registro que contiene el usuario y el código de recuperación en texto plano.
     */
    public record RegistroResult(Usuario usuario, String recoveryCode) {}
}
