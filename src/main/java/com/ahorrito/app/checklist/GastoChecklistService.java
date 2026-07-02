package com.ahorrito.app.checklist;

import com.ahorrito.app.auth.SecurityService;
import com.ahorrito.app.auth.Usuario;
import com.ahorrito.app.category.Categoria;
import com.ahorrito.app.category.CategoriaRepository;
import com.ahorrito.app.exception.ResourceNotFoundException;
import com.ahorrito.app.transaction.TipoTransaccion;
import com.ahorrito.app.transaction.Transaccion;
import com.ahorrito.app.transaction.TransaccionRepository;
import com.ahorrito.app.wallet.Cartera;
import com.ahorrito.app.wallet.CarteraRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GastoChecklistService {

    private final GastoChecklistRepository gastoChecklistRepository;
    private final CarteraRepository carteraRepository;
    private final CategoriaRepository categoriaRepository;
    private final TransaccionRepository transaccionRepository;
    private final GastoChecklistMapper gastoChecklistMapper;
    private final SecurityService securityService;

    @Transactional(readOnly = true)
    public List<GastoChecklistResponseDTO> findAllForMonth(Integer anio, Integer mes) {
        Usuario usuario = securityService.getLoggedUser();
        LocalDate start, end;
        if (anio != null && mes != null) {
            start = LocalDate.of(anio, mes, 1);
            end = start.withDayOfMonth(start.lengthOfMonth());
        } else {
            LocalDate now = LocalDate.now();
            start = now.withDayOfMonth(1);
            end = now.withDayOfMonth(now.lengthOfMonth());
        }
        return gastoChecklistRepository.findAllByUsuarioAndFechaBetween(usuario, start, end).stream()
                .map(gastoChecklistMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@securityService.isGastoChecklistOwner(#id)")
    public GastoChecklistResponseDTO findById(Long id) {
        Usuario usuario = securityService.getLoggedUser();
        GastoChecklist gasto = gastoChecklistRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto proyectado no encontrado con id: " + id));
        return gastoChecklistMapper.toResponseDTO(gasto);
    }

    @Transactional
    @PreAuthorize("@securityService.isCarteraOwner(#requestDTO.carteraId()) and @securityService.isCategoriaOwner(#requestDTO.categoriaId())")
    public GastoChecklistResponseDTO create(GastoChecklistRequestDTO requestDTO) {
        Usuario usuario = securityService.getLoggedUser();

        Cartera cartera = carteraRepository.findByIdAndUsuario(requestDTO.carteraId(), usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Cartera no encontrada con id: " + requestDTO.carteraId()));

        Categoria categoria = categoriaRepository.findByIdAndUsuario(requestDTO.categoriaId(), usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + requestDTO.categoriaId()));

        GastoChecklist gasto = gastoChecklistMapper.toEntity(requestDTO);
        gasto.setUsuario(usuario);
        gasto.setCartera(cartera);
        gasto.setCategoria(categoria);
        gasto.setCompletado(false);

        GastoChecklist saved = gastoChecklistRepository.save(gasto);
        return gastoChecklistMapper.toResponseDTO(saved);
    }

    @Transactional
    @PreAuthorize("@securityService.isGastoChecklistOwner(#id) and @securityService.isCarteraOwner(#requestDTO.carteraId()) and @securityService.isCategoriaOwner(#requestDTO.categoriaId())")
    public GastoChecklistResponseDTO update(Long id, GastoChecklistRequestDTO requestDTO) {
        Usuario usuario = securityService.getLoggedUser();
        GastoChecklist gasto = gastoChecklistRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto proyectado no encontrado con id: " + id));

        Cartera newCartera = carteraRepository.findByIdAndUsuario(requestDTO.carteraId(), usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Cartera no encontrada con id: " + requestDTO.carteraId()));

        Categoria newCategoria = categoriaRepository.findByIdAndUsuario(requestDTO.categoriaId(), usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + requestDTO.categoriaId()));

        // If the item is already completed and linked to a transaction, update the transaction details as well
        if (gasto.isCompletado() && gasto.getTransaccion() != null) {
            Transaccion transaccion = gasto.getTransaccion();
            
            // Reverse old balance effect on old wallet
            Cartera oldCartera = transaccion.getCartera();
            BigDecimal oldMonto = transaccion.getMonto();
            if (oldCartera != null) {
                oldCartera.setMontoActual(oldCartera.getMontoActual().add(oldMonto));
            }

            // Apply new balance effect on new wallet
            newCartera.setMontoActual(newCartera.getMontoActual().subtract(requestDTO.monto()));

            // Update transaction properties
            transaccion.setDescripcion("Pago: " + requestDTO.nombre());
            transaccion.setMonto(requestDTO.monto());
            transaccion.setFecha(requestDTO.fecha());
            transaccion.setCartera(newCartera);
            transaccion.setCategoria(newCategoria);
            
            transaccionRepository.save(transaccion);
        }

        gastoChecklistMapper.updateEntityFromDTO(requestDTO, gasto);
        gasto.setCartera(newCartera);
        gasto.setCategoria(newCategoria);

        return gastoChecklistMapper.toResponseDTO(gasto);
    }

    @Transactional
    @PreAuthorize("@securityService.isGastoChecklistOwner(#id)")
    public GastoChecklistResponseDTO toggleCompleted(Long id) {
        Usuario usuario = securityService.getLoggedUser();
        GastoChecklist gasto = gastoChecklistRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto proyectado no encontrado con id: " + id));

        if (!gasto.isCompletado()) {
            // Mark as completed: Create a GASTO transaction
            Transaccion transaccion = Transaccion.builder()
                    .descripcion("Pago: " + gasto.getNombre())
                    .monto(gasto.getMonto())
                    .fecha(LocalDate.now())
                    .tipo(TipoTransaccion.GASTO)
                    .cartera(gasto.getCartera())
                    .categoria(gasto.getCategoria())
                    .usuario(usuario)
                    .build();

            // Adjust Wallet Balance (deduct Gasto amount)
            Cartera cartera = gasto.getCartera();
            cartera.setMontoActual(cartera.getMontoActual().subtract(gasto.getMonto()));
            carteraRepository.save(cartera);

            Transaccion savedTransaccion = transaccionRepository.save(transaccion);
            gasto.setTransaccion(savedTransaccion);
            gasto.setCompletado(true);
        } else {
            // Unmark as completed: Delete associated GASTO transaction
            Transaccion transaccion = gasto.getTransaccion();
            if (transaccion != null) {
                // Reverse Wallet Balance (refund amount)
                Cartera cartera = transaccion.getCartera();
                if (cartera != null) {
                    cartera.setMontoActual(cartera.getMontoActual().add(transaccion.getMonto()));
                    carteraRepository.save(cartera);
                }
                gasto.setTransaccion(null);
                transaccionRepository.delete(transaccion);
            }
            gasto.setCompletado(false);
        }

        GastoChecklist saved = gastoChecklistRepository.save(gasto);
        return gastoChecklistMapper.toResponseDTO(saved);
    }

    @Transactional
    @PreAuthorize("@securityService.isGastoChecklistOwner(#id)")
    public void delete(Long id) {
        Usuario usuario = securityService.getLoggedUser();
        GastoChecklist gasto = gastoChecklistRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto proyectado no encontrado con id: " + id));

        // If it was completed, delete the transaction and reverse balance first
        if (gasto.isCompletado() && gasto.getTransaccion() != null) {
            Transaccion transaccion = gasto.getTransaccion();
            Cartera cartera = transaccion.getCartera();
            if (cartera != null) {
                cartera.setMontoActual(cartera.getMontoActual().add(transaccion.getMonto()));
                carteraRepository.save(cartera);
            }
            transaccionRepository.delete(transaccion);
        }

        gastoChecklistRepository.delete(gasto);
    }

    /**
     * Runs at 00:00:00 on the first day of every month.
     * Keeps memory footprint close to zero by utilizing single database bulk updates and deletes.
     */
    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void ejecutarReseteoMensual() {
        // Reactivate recurring permanent items
        gastoChecklistRepository.resetPermanentes();

        // Delete non-permanent checklist items from the previous months
        LocalDate inicioMesActual = LocalDate.now().withDayOfMonth(1);
        gastoChecklistRepository.deleteNoPermanentesAnteriores(inicioMesActual);
    }
}
