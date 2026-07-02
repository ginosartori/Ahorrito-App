package com.ahorrito.app.wallet;
import com.ahorrito.app.auth.SecurityService;

import com.ahorrito.app.wallet.CarteraRequestDTO;
import com.ahorrito.app.wallet.CarteraResponseDTO;
import com.ahorrito.app.wallet.Cartera;
import com.ahorrito.app.transaction.Transaccion;
import com.ahorrito.app.auth.Usuario;
import com.ahorrito.app.exception.ResourceNotFoundException;
import com.ahorrito.app.wallet.CarteraMapper;
import com.ahorrito.app.wallet.CarteraRepository;
import com.ahorrito.app.transaction.TransaccionRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarteraService {

    private final CarteraRepository carteraRepository;
    private final TransaccionRepository transaccionRepository;
    private final CarteraMapper carteraMapper;
    private final SecurityService securityService;

    @Transactional(readOnly = true)
    public List<CarteraResponseDTO> findAll() {
        Usuario usuario = securityService.getLoggedUser();
        return carteraRepository.findAllByUsuarioOrderByOrdenAscIdAsc(usuario).stream()
                .map(carteraMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@securityService.isCarteraOwner(#id)")
    public CarteraResponseDTO findById(Long id) {
        Usuario usuario = securityService.getLoggedUser();
        Cartera cartera = carteraRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Cartera no encontrada con id: " + id));
        return carteraMapper.toResponseDTO(cartera);
    }

    @Transactional
    public CarteraResponseDTO create(CarteraRequestDTO requestDTO) {
        Usuario usuario = securityService.getLoggedUser();
        Cartera cartera = carteraMapper.toEntity(requestDTO);
        cartera.setUsuario(usuario);
        Integer maxOrden = carteraRepository.findMaxOrdenByUsuario(usuario);
        cartera.setOrden(maxOrden != null ? maxOrden + 1 : 0);
        Cartera saved = carteraRepository.save(cartera);
        return carteraMapper.toResponseDTO(saved);
    }

    @Transactional
    @PreAuthorize("@securityService.isCarteraOwner(#id)")
    public CarteraResponseDTO update(Long id, CarteraRequestDTO requestDTO) {
        Usuario usuario = securityService.getLoggedUser();
        Cartera cartera = carteraRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Cartera no encontrada con id: " + id));

        carteraMapper.updateEntityFromDTO(requestDTO, cartera);
        return carteraMapper.toResponseDTO(cartera);
    }

    @Transactional
    @PreAuthorize("@securityService.isCarteraOwnerList(#ids)")
    public void reorder(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        Usuario usuario = securityService.getLoggedUser();
        List<Cartera> carteras = carteraRepository.findAllByUsuarioOrderByOrdenAscIdAsc(usuario);
        
        java.util.Map<Long, Cartera> carteraMap = carteras.stream()
                .collect(Collectors.toMap(Cartera::getId, java.util.function.Function.identity()));

        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            Cartera cartera = carteraMap.get(id);
            if (cartera != null) {
                cartera.setOrden(i);
            }
        }
        carteraRepository.saveAll(carteras);
    }

    @Transactional
    @PreAuthorize("@securityService.isCarteraOwner(#id)")
    public void delete(Long id) {
        Usuario usuario = securityService.getLoggedUser();
        Cartera cartera = carteraRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Cartera no encontrada con id: " + id));

        // Delete all associated transactions to avoid FK constraints violation in a single query
        transaccionRepository.deleteByCarteraId(id);

        carteraRepository.delete(cartera);
    }
}
