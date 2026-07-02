package com.ahorrito.app.profile;
import com.ahorrito.app.auth.SecurityService;

import com.ahorrito.app.profile.PerfilDTO;
import com.ahorrito.app.profile.Perfil;
import com.ahorrito.app.profile.PerfilMapper;
import com.ahorrito.app.profile.PerfilRepository;
import com.ahorrito.app.auth.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PerfilService {

    private final PerfilRepository perfilRepository;
    private final PerfilMapper perfilMapper;
    private final SecurityService securityService;

    @Transactional
    public PerfilDTO getPerfil() {
        Usuario usuario = securityService.getLoggedUser();
        Perfil perfil = perfilRepository.findByUsuario(usuario).orElseGet(() -> {
            Perfil defaultPerfil = Perfil.builder()
                    .nombre(usuario.getUsername())
                    .rango("Miembro Premium")
                    .tema("classic-dark")
                    .usuario(usuario)
                    .build();
            return perfilRepository.save(defaultPerfil);
        });
        return perfilMapper.toDTO(perfil);
    }

    @Transactional
    public PerfilDTO updatePerfil(PerfilDTO requestDTO) {
        Usuario usuario = securityService.getLoggedUser();
        Perfil perfil = perfilRepository.findByUsuario(usuario).orElseGet(() -> {
            Perfil defaultPerfil = Perfil.builder()
                    .nombre(usuario.getUsername())
                    .rango("Miembro Premium")
                    .tema("classic-dark")
                    .usuario(usuario)
                    .build();
            return perfilRepository.save(defaultPerfil);
        });
        
        perfilMapper.updateEntityFromDTO(requestDTO, perfil);
        Perfil saved = perfilRepository.save(perfil);
        return perfilMapper.toDTO(saved);
    }
}
