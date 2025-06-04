package co.edu.uco.backend.businesslogic.facade.impl;

import co.edu.uco.backend.businesslogic.assembler.reserva.dto.ReservaDTOAssembler;
import co.edu.uco.backend.businesslogic.businesslogic.domain.ReservaDomain;
import co.edu.uco.backend.businesslogic.businesslogic.impl.ReservaBusinessLogicImpl;
import co.edu.uco.backend.businesslogic.businesslogic.ReservaBusinessLogic;
import co.edu.uco.backend.businesslogic.facade.ReservaFacade;
import co.edu.uco.backend.crosscutting.exceptions.BackEndException;
import co.edu.uco.backend.crosscutting.exceptions.BusinessLogicBackEndException;
import co.edu.uco.backend.data.dao.factory.DAOFactory;
import co.edu.uco.backend.data.dao.factory.Factory;
import co.edu.uco.backend.dto.ReservaDTO;
import co.edu.uco.backend.entity.CanchaEntity;
import co.edu.uco.backend.entity.ClienteEntity;
import co.edu.uco.backend.entity.EstadoReservaEntity;

import java.util.*;

public class ReservaFacadeImpl implements ReservaFacade {

    private final DAOFactory daoFactory;
    private final ReservaBusinessLogic reservaBusinessLogic;

    public ReservaFacadeImpl() throws BackEndException {
        daoFactory = DAOFactory.getFactory(Factory.POSTGRE_SQL);
        reservaBusinessLogic = new ReservaBusinessLogicImpl(daoFactory);
    }


    @Override
    public void registrarNuevaReserva(UUID clienteID, ReservaDTO reserva) {
        //Sin implementar
    }

    @Override
    public void confirmarReserva(UUID clienteId, UUID idReserva, ReservaDTO reserva) {
        //Sin implementar
    }

    @Override
    public void cancelarReservaPorCliente(UUID clienteId, UUID reservaId, ReservaDTO reserva) {
        //Sin implementar
    }

    @Override
    public ReservaDTO consultarReservaPorCliente(UUID clienteId, UUID reservaId) {
        return null;
    }

    @Override
    public List<Map<String,Object>> listarReservasPorCliente(UUID clienteId, ReservaDTO filtro) throws BackEndException {
        daoFactory.abrirConexion();
        try {
            // 1) Convertir el filtro DTO a domain (para pasar a la BL)
            ReservaDomain filtroDomain = ReservaDTOAssembler.getInstance().toDomain(filtro);

            // 2) Invocar al business logic para que traiga la lista de dominios
            List<ReservaDomain> dominios = reservaBusinessLogic.listarReservasPorCliente(clienteId, filtroDomain);

            // 3) Construir salida “ligera”: un Map<String,Object> por cada reserva
            List<Map<String, Object>> salida = new ArrayList<>();

            for (ReservaDomain dom : dominios) {
                Map<String, Object> fila = new HashMap<>();

                // a) Campos básicos que vienen en-domain:
                fila.put("codigoreserva", dom.getId());
                fila.put("fechaReserva", dom.getFechaReserva());
                fila.put("fechaUsoCancha", dom.getFechaUsoCancha());
                fila.put("horaInicio", dom.getHoraInicio());
                fila.put("horaFin", dom.getHoraFin());

                // b) Inyectar “nombreCliente” mediante DAO
                UUID clienteUUID = dom.getCliente().getId();
                ClienteEntity clienteEntity = daoFactory.getClienteDAO().consultarPorId(clienteUUID);
                fila.put("nombreCliente", clienteEntity.getNombre());

                // c) Inyectar “nombreCancha” mediante DAO
                UUID canchaUUID = dom.getCancha().getId();
                CanchaEntity canchaEntity = daoFactory.getCanchaDAO().consultarPorId(canchaUUID);
                fila.put("nombreCancha", canchaEntity.getNombreCancha());

                // d) Inyectar “nombreEstado” mediante DAO
                UUID estadoUUID = dom.getEstado().getId();
                EstadoReservaEntity estadoEntity = daoFactory.getEstadoReservaDAO().consultarPorId(estadoUUID);
                fila.put("nombreEstado", estadoEntity.getNombre());

                salida.add(fila);
            }

            return salida;

        } catch (BackEndException ex) {
            throw ex;
        } catch (Exception ex) {
            var mensajeUsuario  = "Se ha presentado un problema inesperado al listar reservas por cliente";
            var mensajeTecnico  = "Excepción inesperada listando reservas por cliente";
            throw BusinessLogicBackEndException.reportar(mensajeUsuario, mensajeTecnico, ex);
        } finally {
            daoFactory.cerrarConexion();
        }
    }

    @Override
    public void finalizarReserva(UUID clienteId, UUID reservaId) {
        //Sin implementar
    }

    @Override
    public void cancelarReservaPorOrganizacion(UUID orgId, UUID reservaId) {
        //Sin implementar
    }

    @Override
    public List<ReservaDTO> listarReservasPorCancha(UUID orgId, UUID canchaId) {
        return List.of();
    }
}
