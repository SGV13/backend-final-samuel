package co.edu.uco.backend.businesslogic.facade.impl;

import co.edu.uco.backend.businesslogic.assembler.resena.dto.ResenaDTOAssembler;
import co.edu.uco.backend.businesslogic.businesslogic.domain.ResenaDomain;
import co.edu.uco.backend.businesslogic.businesslogic.impl.ResenaBusinessLogicImpl;
import co.edu.uco.backend.businesslogic.businesslogic.ResenaBusinessLogic;
import co.edu.uco.backend.businesslogic.facade.ResenaFacade;
import co.edu.uco.backend.crosscutting.exceptions.BackEndException;
import co.edu.uco.backend.crosscutting.exceptions.BusinessLogicBackEndException;
import co.edu.uco.backend.data.dao.factory.DAOFactory;
import co.edu.uco.backend.data.dao.factory.Factory;
import co.edu.uco.backend.dto.ResenaDTO;

import java.util.List;
import java.util.UUID;

public class ResenaFacadeImpl implements ResenaFacade {

    private final DAOFactory daoFactory;
    private final ResenaBusinessLogic resenaBusinessLogic;

    public ResenaFacadeImpl() throws BackEndException {
        daoFactory = DAOFactory.getFactory(Factory.POSTGRE_SQL);
        resenaBusinessLogic = new ResenaBusinessLogicImpl(daoFactory);
    }


    @Override
    public void registrarNuevaResena(UUID idReserva, ResenaDTO resena) throws BackEndException {
        daoFactory.abrirConexion();
        try {
            daoFactory.iniciarTransaccion();

            ResenaDomain resenaDomain = ResenaDTOAssembler.getInstance().toDomain(resena);
            resenaBusinessLogic.registrarNuevaResena(idReserva, resenaDomain);

            daoFactory.confirmarTransaccion();
        } catch (BackEndException exception) {
            daoFactory.cancelarTransaccion();
            throw exception;
        } catch (Exception exception) {
            daoFactory.cancelarTransaccion();
            var mensajeTecnico = "Se presentó una excepción inesperada de tipo Exception tratando de registrar la información de la nueva reseña, para más detalles revise el log de errores";
            var mensajeUsuario = "Se ha presentado un problema inesperado tratando de registrar la información de la nueva reseña";

            throw BusinessLogicBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        } finally {
            daoFactory.cerrarConexion();
        }
    }

    @Override
    public void modificarResenaExistente(UUID reservaId, UUID resenaId, ResenaDTO resena) throws BackEndException {
        daoFactory.abrirConexion();
        try {
            daoFactory.iniciarTransaccion();

            ResenaDomain resenaDomain = ResenaDTOAssembler.getInstance().toDomain(resena);
            resenaBusinessLogic.modificarResenaExistente(reservaId,resenaId, resenaDomain);

            daoFactory.confirmarTransaccion();
        } catch (BackEndException exception) {
            daoFactory.cancelarTransaccion();
            throw exception;
        } catch (Exception exception) {
            daoFactory.cancelarTransaccion();
            var mensajeTecnico = "Se presentó una excepción inesperada de tipo Exception tratando de modificar la información de la nueva reseña, para más detalles revise el log de errores";
            var mensajeUsuario = "Se ha presentado un problema inesperado tratando de modificar la información de la reseña";

            throw BusinessLogicBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        } finally {
            daoFactory.cerrarConexion();
        }
    }

    @Override
    public void darBajaDefinitivamenteResenaExistente(UUID reservaId, UUID resenaId) throws BackEndException {
        daoFactory.abrirConexion();
        try {
            daoFactory.iniciarTransaccion();

            resenaBusinessLogic.darBajaDefinitivamenteResenaExistente(reservaId, resenaId);

            daoFactory.confirmarTransaccion();
        } catch (BackEndException exception) {
            daoFactory.cancelarTransaccion();
            throw exception;
        } catch (Exception exception) {
            daoFactory.cancelarTransaccion();
            var mensajeTecnico = "Se presentó una excepción inesperada de tipo Exception tratando de eliminar la información de la reseña, para más detalles revise el log de errores";
            var mensajeUsuario = "Se ha presentado un problema inesperado tratando de eliminar la información de la nueva reseña";

            throw BusinessLogicBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        } finally {
            daoFactory.cerrarConexion();
        }
    }

    @Override
    public ResenaDTO consultarResenaPorReserva(UUID reservaId, UUID resenaId) throws BackEndException {
        daoFactory.abrirConexion();
        try {
            var resenaDomainResultado = resenaBusinessLogic.consultarResenaPorReserva(reservaId,resenaId);
            return ResenaDTOAssembler.getInstance().toDTO(resenaDomainResultado);
        } catch (BackEndException exception) {
            throw exception;
        } catch (Exception exception) {
            var mensajeTecnico = "Se presentó una excepción inesperada de tipo Exception tratando de consultar la información de la reseña perteneciente a la reserva deseada, para más detalles revise el log de errores";
            var mensajeUsuario = "Se ha presentado un problema inesperado tratando de consultar la información de la reseña perteneciente a la reserva deseada...";

            throw BusinessLogicBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        } finally {
            daoFactory.cerrarConexion();
        }
    }

    @Override
    public List<ResenaDTO> consultarResenas(UUID reservaId, ResenaDTO filtro) throws BackEndException {
        daoFactory.abrirConexion();
        try {

            ResenaDomain filtroDomain = ResenaDTOAssembler.getInstance().toDomain(filtro);
            List<ResenaDomain> dominios = resenaBusinessLogic.consultarResenas(reservaId,filtroDomain);
            return ResenaDTOAssembler.getInstance().toDTOs(dominios);
        } catch (BackEndException ex) {
            throw ex;
        } catch (Exception ex) {
            var mensajeUsuario = "Se ha presentado un problema inesperado al consultar la reseña";
            var mensajeTecnico = "Excepción inesperada listando todas las reseñas";
            throw BusinessLogicBackEndException.reportar(mensajeUsuario, mensajeTecnico, ex);
        } finally {
            daoFactory.cerrarConexion();
        }
    }
}
