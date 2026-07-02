package com.ahorrito.app.transaction;
import com.ahorrito.app.auth.SecurityService;

import com.ahorrito.app.transaction.TransaccionRequestDTO;
import com.ahorrito.app.transaction.TransaccionResponseDTO;
import com.ahorrito.app.category.Categoria;
import com.ahorrito.app.wallet.Cartera;
import com.ahorrito.app.transaction.Transaccion;
import com.ahorrito.app.transaction.TipoTransaccion;
import com.ahorrito.app.auth.Usuario;
import com.ahorrito.app.exception.ResourceNotFoundException;
import com.ahorrito.app.transaction.TransaccionMapper;
import com.ahorrito.app.category.CategoriaRepository;
import com.ahorrito.app.wallet.CarteraRepository;
import com.ahorrito.app.transaction.TransaccionRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final CategoriaRepository categoriaRepository;
    private final CarteraRepository carteraRepository;
    private final TransaccionMapper transaccionMapper;
    private final SecurityService securityService;

    @Transactional(readOnly = true)
    public List<TransaccionResponseDTO> findAll() {
        Usuario usuario = securityService.getLoggedUser();
        return transaccionRepository.findAllByUsuario(usuario).stream()
                .map(transaccionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@securityService.isTransaccionOwner(#id)")
    public TransaccionResponseDTO findById(Long id) {
        Usuario usuario = securityService.getLoggedUser();
        Transaccion transaccion = transaccionRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada con id: " + id));
        return transaccionMapper.toResponseDTO(transaccion);
    }

    @Transactional
    @PreAuthorize("@securityService.isCarteraOwner(#requestDTO.carteraId()) and @securityService.isCategoriaOwner(#requestDTO.categoriaId())")
    public TransaccionResponseDTO create(TransaccionRequestDTO requestDTO) {
        Usuario usuario = securityService.getLoggedUser();
        if (requestDTO.carteraId() == null) {
            throw new IllegalArgumentException("El ID de la cartera es obligatorio");
        }

        Cartera cartera = carteraRepository.findByIdAndUsuario(requestDTO.carteraId(), usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Cartera no encontrada con id: " + requestDTO.carteraId()));

        Categoria categoria = null;
        if (requestDTO.categoriaId() != null) {
            categoria = categoriaRepository.findByIdAndUsuario(requestDTO.categoriaId(), usuario)
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + requestDTO.categoriaId()));
        }

        Transaccion transaccion = transaccionMapper.toEntity(requestDTO);
        transaccion.setCartera(cartera);
        transaccion.setCategoria(categoria);
        transaccion.setUsuario(usuario);

        // Adjust wallet balance
        adjustCarteraBalance(cartera, transaccion.getMonto(), transaccion.getTipo(), false);

        Transaccion saved = transaccionRepository.save(transaccion);
        return transaccionMapper.toResponseDTO(saved);
    }

    @Transactional
    @PreAuthorize("@securityService.isTransaccionOwner(#id) and @securityService.isCarteraOwner(#requestDTO.carteraId()) and @securityService.isCategoriaOwner(#requestDTO.categoriaId())")
    public TransaccionResponseDTO update(Long id, TransaccionRequestDTO requestDTO) {
        Usuario usuario = securityService.getLoggedUser();
        Transaccion transaccion = transaccionRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada con id: " + id));

        if (requestDTO.carteraId() == null) {
            throw new IllegalArgumentException("El ID de la cartera es obligatorio");
        }

        Cartera oldCartera = transaccion.getCartera();
        BigDecimal oldMonto = transaccion.getMonto();
        TipoTransaccion oldTipo = transaccion.getTipo();

        // 1. Reverse old transaction effect
        if (oldCartera != null) {
            adjustCarteraBalance(oldCartera, oldMonto, oldTipo, true);
        }

        // 2. Apply new transaction effect
        Cartera newCartera = carteraRepository.findByIdAndUsuario(requestDTO.carteraId(), usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Cartera no encontrada con id: " + requestDTO.carteraId()));

        Categoria newCategoria = null;
        if (requestDTO.categoriaId() != null) {
            newCategoria = categoriaRepository.findByIdAndUsuario(requestDTO.categoriaId(), usuario)
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + requestDTO.categoriaId()));
        }

        transaccionMapper.updateEntityFromDTO(requestDTO, transaccion);
        transaccion.setCartera(newCartera);
        transaccion.setCategoria(newCategoria);

        adjustCarteraBalance(newCartera, transaccion.getMonto(), transaccion.getTipo(), false);

        return transaccionMapper.toResponseDTO(transaccion);
    }

    @Transactional
    @PreAuthorize("@securityService.isTransaccionOwner(#id)")
    public void delete(Long id) {
        Usuario usuario = securityService.getLoggedUser();
        Transaccion transaccion = transaccionRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada con id: " + id));

        Cartera cartera = transaccion.getCartera();
        if (cartera != null) {
            // Reverse transaction effect
            adjustCarteraBalance(cartera, transaccion.getMonto(), transaccion.getTipo(), true);
        }

        transaccionRepository.delete(transaccion);
    }

    private void adjustCarteraBalance(Cartera cartera, BigDecimal monto, TipoTransaccion tipo, boolean isReverse) {
        BigDecimal balance = cartera.getMontoActual();
        if (tipo == TipoTransaccion.INGRESO || tipo == TipoTransaccion.AHORRO) {
            if (isReverse) {
                balance = balance.subtract(monto);
            } else {
                balance = balance.add(monto);
            }
        } else if (tipo == TipoTransaccion.GASTO) {
            if (isReverse) {
                balance = balance.add(monto);
            } else {
                balance = balance.subtract(monto);
            }
        }
        cartera.setMontoActual(balance);
    }
}
