package co.edu.uco.backend.businesslogic.businesslogic.impl;

import co.edu.uco.backend.businesslogic.assembler.reserva.entity.ReservaEntityAssembler;
import co.edu.uco.backend.businesslogic.businesslogic.ReservaBusinessLogic;
import co.edu.uco.backend.businesslogic.businesslogic.domain.ReservaDomain;
import co.edu.uco.backend.crosscutting.exceptions.BackEndException;
import co.edu.uco.backend.crosscutting.exceptions.BusinessLogicBackEndException;
import co.edu.uco.backend.data.dao.factory.DAOFactory;
import co.edu.uco.backend.entity.ReservaEntity;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReservaBusinessLogicImpl implements ReservaBusinessLogic {

    private final DAOFactory factory;
    public ReservaBusinessLogicImpl(DAOFactory factory) {
        this.factory = factory;
    }

    @Override
    public void registrarNuevaReserva(UUID clienteID, ReservaDomain reserva) throws BackEndException {
        // Validar cliente
        ReservaEntity reservaEntity = null;
        factory.getReservaDAO().crear(reservaEntity);
    }

    @Override
    public void confirmarReserva(UUID clienteId, UUID idReserva, ReservaDomain reserva) throws BackEndException {
        //Validar cliente
        ReservaEntity reservaEntity = null;
        factory.getReservaDAO().modificar(idReserva,reservaEntity);
    }


    @Override
    public void cancelarReservaPorCliente(UUID clienteId, UUID reservaId, ReservaDomain reserva) throws BackEndException {
        //Validar cliente
        ReservaEntity reservaEntity = null;
        factory.getReservaDAO().modificar(reservaId,reservaEntity);
    }

    @Override
    public ReservaDomain consultarReservaPorCliente(UUID clienteId, UUID reservaId) throws BackEndException {
        //Validar cliente
        ReservaEntity reservaEntity = null;
        factory.getReservaDAO().consultarPorId(reservaId);
        return null;
    }

    @Override
    public List<ReservaDomain> listarReservasPorCliente(UUID clienteId, ReservaDomain filtro) throws BackEndException {
        if (clienteId == null) {
            throw BusinessLogicBackEndException.reportar("El ID de cliente no puede ser nulo.");
        }
        List<ReservaEntity> entities = factory.getReservaDAO().consultarPorCliente(clienteId);
        // Convertir cada entidad a dominio
        return entities.stream()
                .map(ReservaEntityAssembler.getInstance()::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void finalizarReserva(UUID clienteId, UUID reservaId) {
        //Implementar logica
    }


    @Override
    public void cancelarReservaPorOrganizacion(UUID orgId, UUID reservaId) throws BackEndException {
        ReservaEntity reservaEntity = null;
        //Validar organizacion y encargado
        factory.getReservaDAO().modificar(reservaId,reservaEntity);
    }

    @Override
    public List<ReservaDomain> listarReservasPorCancha(UUID orgId, UUID canchaId) throws BackEndException {
        ReservaEntity reservaFilter = null;
        //Validar organizacion y cancha
        List<ReservaEntity> reservaEntities = factory.getReservaDAO().consultar(reservaFilter);
        List<ReservaDomain> datosARetornar = null;
        return datosARetornar;
    }


}
