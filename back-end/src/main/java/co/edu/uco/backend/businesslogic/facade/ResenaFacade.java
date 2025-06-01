package co.edu.uco.backend.businesslogic.facade;

import co.edu.uco.backend.crosscutting.exceptions.BackEndException;
import co.edu.uco.backend.dto.ResenaDTO;

import java.util.List;
import java.util.UUID;

public interface ResenaFacade {

    void registrarNuevaResena(UUID reserva, ResenaDTO resena) throws BackEndException;

    void modificarResenaExistente(UUID reservaId, UUID resenaId, ResenaDTO resena) throws BackEndException;

    void darBajaDefinitivamenteResenaExistente(UUID reservaId, UUID resenaId) throws BackEndException;

    ResenaDTO consultarResenaPorReserva(UUID reservaId, UUID resenaId) throws BackEndException;

    List<ResenaDTO> consultarResenas(UUID reservaId, ResenaDTO filtro) throws BackEndException;

}
