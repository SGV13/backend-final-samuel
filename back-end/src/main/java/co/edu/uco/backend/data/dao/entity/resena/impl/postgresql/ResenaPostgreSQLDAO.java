package co.edu.uco.backend.data.dao.entity.resena.impl.postgresql;

import co.edu.uco.backend.crosscutting.exceptions.BackEndException;
import co.edu.uco.backend.crosscutting.exceptions.DataBackEndException;
import co.edu.uco.backend.crosscutting.utilitarios.UtilUUID;
import co.edu.uco.backend.data.dao.entity.resena.ResenaDAO;
import co.edu.uco.backend.entity.ResenaEntity;
import co.edu.uco.backend.entity.ReservaEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ResenaPostgreSQLDAO implements ResenaDAO {

    private final Connection connection;

    public ResenaPostgreSQLDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void crear(ResenaEntity entity) throws BackEndException {
        var sql = new StringBuilder();
        sql.append(
                "INSERT INTO doodb.resena(" +
                        "codigoresena, codigoreserva, calificacion, comentario, fecha" +
                        ") VALUES (?, ?, ?, ?, ?)"
        );

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            // 1) codigoresena
            ps.setObject(1, entity.getId());

            // 2) codigoreserva (solo el UUID de la reserva)
            ps.setObject(2, entity.getReserva().getId());

            // 3) calificacion
            ps.setInt(3, entity.getCalificacion());

            // 4) comentario
            ps.setString(4, entity.getComentario());

            // 5) fecha (LocalDate)
            ps.setObject(5, entity.getFecha());

            ps.executeUpdate();
        } catch (SQLException exception) {
            var mensajeTecnico = "Se presentó una SQLException tratando de registrar la nueva información de la reseña en la base de datos";
            var mensajeUsuario = "No se pudo registrar la reseña en este momento";
            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        } catch (Exception exception) {
            var mensajeTecnico = "Se presentó una excepción NO CONTROLADA tratando de ingresar la nueva información de la reseña en la base de datos";
            var mensajeUsuario = "Ha ocurrido un problema inesperado al registrar la reseña";
            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        }
    }

    @Override
    public void eliminar(UUID resenaId) throws BackEndException {
        var sql = new StringBuilder();
        sql.append("DELETE FROM doodb.resena WHERE codigoresena = ?");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            ps.setObject(1, resenaId);
            ps.executeUpdate();
        } catch (SQLException exception) {
            var mensajeTecnico = "Se presentó una SQLException tratando de eliminar la reseña de la base de datos";
            var mensajeUsuario = "No se pudo eliminar la reseña en este momento";
            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        } catch (Exception exception) {
            var mensajeTecnico = "Se presentó una excepción NO CONTROLADA tratando de eliminar la reseña de la base de datos";
            var mensajeUsuario = "Ha ocurrido un problema inesperado al eliminar la reseña";
            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        }
    }

    @Override
    public List<ResenaEntity> consultar(ResenaEntity entity) {
        // Por ahora no se usa; devolvemos lista vacía
        return List.of();
    }

    @Override
    public ResenaEntity consultarPorId(UUID id) throws BackEndException {
        var resenaEntityRetorno = new ResenaEntity();
        var sql = new StringBuilder();
        sql.append(
                "SELECT " +
                        "codigoresena, codigoreserva, calificacion, comentario, fecha " +
                        "FROM doodb.resena " +
                        "WHERE codigoresena = ?"
        );

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // 1) ID de la reseña
                    UUID resenaUUID = UtilUUID.convertirAUUID(rs.getString("codigoresena"));
                    resenaEntityRetorno.setId(resenaUUID);

                    // 2) Reserva (solo su ID)
                    UUID reservaUUID = UtilUUID.convertirAUUID(rs.getString("codigoreserva"));
                    ReservaEntity reserva = new ReservaEntity();
                    reserva.setId(reservaUUID);
                    resenaEntityRetorno.setReserva(reserva);

                    // 3) calificacion
                    resenaEntityRetorno.setCalificacion(rs.getInt("calificacion"));

                    // 4) comentario
                    resenaEntityRetorno.setComentario(rs.getString("comentario"));

                    // 5) fecha (DATE)
                    LocalDate fechaRe = rs.getObject("fecha", LocalDate.class);
                    resenaEntityRetorno.setFecha(fechaRe);
                }
            }
        } catch (SQLException exception) {
            var mensajeTecnico = "Se presentó una SQLException tratando de consultar la reseña por ID";
            var mensajeUsuario = "No se pudo consultar la reseña en este momento";
            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        } catch (Exception exception) {
            var mensajeTecnico = "Se presentó una excepción NO CONTROLADA tratando de consultar la reseña por ID";
            var mensajeUsuario = "Ha ocurrido un problema inesperado al consultar la reseña";
            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        }

        return resenaEntityRetorno;
    }

    @Override
    public void modificar(UUID resenaId, ResenaEntity entity) throws BackEndException {
        var sql = new StringBuilder();
        sql.append(
                "UPDATE doodb.resena SET " +
                        "codigoreserva = ?, calificacion = ?, comentario = ?, fecha = ? " +
                        "WHERE codigoresena = ?"
        );

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            // 1) codigoreserva (UUID de la reserva asociada)
            ps.setObject(1, entity.getReserva().getId());

            // 2) calificacion
            ps.setInt(2, entity.getCalificacion());

            // 3) comentario
            ps.setString(3, entity.getComentario());

            // 4) fecha
            ps.setObject(4, entity.getFecha());

            // 5) WHERE codigoresena = resenaId
            ps.setObject(5, resenaId);

            ps.executeUpdate();
        } catch (SQLException exception) {
            var mensajeTecnico = "Se presentó una SQLException tratando de modificar la reseña en la base de datos";
            var mensajeUsuario = "No se pudo actualizar la reseña en este momento";
            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        } catch (Exception exception) {
            var mensajeTecnico = "Se presentó una excepción NO CONTROLADA tratando de modificar la reseña en la base de datos";
            var mensajeUsuario = "Ha ocurrido un problema inesperado al actualizar la reseña";
            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        }
    }

    @Override
    public List<ResenaEntity> consultarPorReserva(UUID reservaId) throws BackEndException {
        var listaResultados = new ArrayList<ResenaEntity>();
        var sql = new StringBuilder();
        sql.append(
                "SELECT codigoresena, codigoreserva, calificacion, comentario, fecha " +
                        "FROM doodb.resena WHERE codigoreserva = ?"
        );
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            ps.setObject(1, reservaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    var entidad = new ResenaEntity();
                    entidad.setId(UtilUUID.convertirAUUID(rs.getString("codigoresena")));
                    ReservaEntity remota = new ReservaEntity();
                    remota.setId(UtilUUID.convertirAUUID(rs.getString("codigoreserva")));
                    entidad.setReserva(remota);

                    entidad.setCalificacion(rs.getInt("calificacion"));
                    entidad.setComentario(rs.getString("comentario"));
                    entidad.setFecha(rs.getObject("fecha", LocalDate.class));
                    listaResultados.add(entidad);
                }
            }
        } catch (SQLException exception) {
            var mensajeTecnico = "Se presentó una SQLException tratando de listar reseñas por reserva";
            var mensajeUsuario = "No se pudo consultar las reseñas de esa reserva en este momento";
            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        }
        return listaResultados;
    }
}

