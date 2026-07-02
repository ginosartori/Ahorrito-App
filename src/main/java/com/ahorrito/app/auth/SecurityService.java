package com.ahorrito.app.auth;

import com.ahorrito.app.wallet.CarteraRepository;
import com.ahorrito.app.category.CategoriaRepository;
import com.ahorrito.app.calendar.EventoCalendarioRepository;
import com.ahorrito.app.transaction.TransaccionRepository;
import com.ahorrito.app.checklist.GastoChecklistRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.repository.JpaRepository;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UsuarioRepository usuarioRepository;
    private final CarteraRepository carteraRepository;
    private final CategoriaRepository categoriaRepository;
    private final TransaccionRepository transaccionRepository;
    private final EventoCalendarioRepository eventoRepository;
    private final GastoChecklistRepository gastoChecklistRepository;

    public Usuario getLoggedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    public Long getLoggedUserId() {
        org.springframework.security.core.Authentication authentication = 
                SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getId();
        }
        // Fallback for tests using @WithMockUser
        String username = authentication.getName();
        if (username != null && !username.isEmpty()) {
            return usuarioRepository.findByUsername(username)
                    .map(Usuario::getId)
                    .orElse(null);
        }
        return null;
    }

    private <T extends UserOwnedEntity> boolean checkOwnership(JpaRepository<T, Long> repository, Long id) {
        if (id == null) return true;
        Long loggedUserId = getLoggedUserId();
        if (loggedUserId == null) return false;
        return repository.findById(id)
                .map(entity -> entity.getUsuario().getId().equals(loggedUserId))
                .orElse(true);
    }

    public boolean isCarteraOwner(Long id) {
        return checkOwnership(carteraRepository, id);
    }

    public boolean isCarteraOwnerList(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return true;
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        long distinctCount = ids.stream().distinct().count();
        long ownedCount = carteraRepository.countByIdInAndUsuarioUsername(ids, username);
        return distinctCount == ownedCount;
    }

    public boolean isCategoriaOwner(Long id) {
        return checkOwnership(categoriaRepository, id);
    }

    public boolean isTransaccionOwner(Long id) {
        return checkOwnership(transaccionRepository, id);
    }

    public boolean isEventoOwner(Long id) {
        return checkOwnership(eventoRepository, id);
    }

    public boolean isGastoChecklistOwner(Long id) {
        return checkOwnership(gastoChecklistRepository, id);
    }
}
