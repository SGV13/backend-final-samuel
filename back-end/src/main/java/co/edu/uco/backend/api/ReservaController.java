package co.edu.uco.backend.api;

import co.edu.uco.backend.businesslogic.facade.ReservaFacade;
import co.edu.uco.backend.businesslogic.facade.impl.ReservaFacadeImpl;
import co.edu.uco.backend.crosscutting.exceptions.BackEndException;
import co.edu.uco.backend.dto.ReservaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clientes/{clienteId}/reservas")
public class ReservaController {

    private final ReservaFacade reservaFacade;

    public ReservaController() throws BackEndException {
        this.reservaFacade = new ReservaFacadeImpl();
    }

    @GetMapping("/dummy")
    public ReservaDTO dummy() {
        return new ReservaDTO();
    }

    @PostMapping
    public ResponseEntity<String> registrar(
            @PathVariable UUID clienteId,
            @RequestBody ReservaDTO reserva) {
        reservaFacade.registrarNuevaReserva(clienteId, reserva);
        return new ResponseEntity<>("Reserva registrada exitosamente.", HttpStatus.CREATED);
    }

    @PutMapping("/{reservaId}/confirmar")
    public ResponseEntity<String> confirmar(
            @PathVariable UUID clienteId,
            @PathVariable UUID reservaId,
            @RequestBody ReservaDTO reserva){
        reservaFacade.confirmarReserva(clienteId, reservaId, reserva);
        return new ResponseEntity<>("Reserva confirmada exitosamente.", HttpStatus.OK);
    }

    @PutMapping("/{reservaId}/cancelar")
    public ResponseEntity<String> cancelarCliente(
            @PathVariable UUID clienteId,
            @PathVariable UUID reservaId,
            @RequestBody ReservaDTO reserva) {
        reservaFacade.cancelarReservaPorCliente(clienteId, reservaId, reserva);
        return new ResponseEntity<>("Reserva cancelada por el cliente.", HttpStatus.OK);
    }

    @GetMapping("/{reservaId}")
    public ResponseEntity<ReservaDTO> consultarPorId(
            @PathVariable UUID clienteId,
            @PathVariable UUID reservaId){
        var reservaDto = reservaFacade.consultarReservaPorCliente(clienteId, reservaId);
        return new ResponseEntity<>(reservaDto, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<ReservaDTO>> listar(
            @PathVariable UUID clienteId) throws BackEndException {
        // Usamos un DTO vacío como filtro por defecto
        var lista = reservaFacade.listarReservasPorCliente(clienteId, new ReservaDTO());
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }

    @PutMapping("/{reservaId}/finalizar")
    public ResponseEntity<String> finalizar(
            @PathVariable UUID clienteId,
            @PathVariable UUID reservaId) {
        reservaFacade.finalizarReserva(clienteId, reservaId);
        return new ResponseEntity<>("Reserva finalizada exitosamente.", HttpStatus.OK);
    }
}
