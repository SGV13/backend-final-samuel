package co.edu.uco.backend.data.dao.entity.estadoreserva.impl.postgresql;

import co.edu.uco.backend.crosscutting.exceptions.BackEndException;
import co.edu.uco.backend.crosscutting.exceptions.DataBackEndException;
import co.edu.uco.backend.crosscutting.utilitarios.UtilUUID;
import co.edu.uco.backend.data.dao.entity.estadoreserva.EstadoReservaDAO;
import co.edu.uco.backend.entity.EstadoReservaEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class EstadoReservaPostgreSQLDAO implements EstadoReservaDAO {

    private final Connection connection;

    public EstadoReservaPostgreSQLDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void crear(EstadoReservaEntity entity) throws BackEndException {
        var sentenciaSQL = new StringBuilder();
        sentenciaSQL.append("INSERT INTO cancha(id, nombre)" +
                " VALUES (?, ?)");
        try (var sentenciaPreparada = connection.prepareStatement(sentenciaSQL.toString())){
            sentenciaPreparada.setObject(1,entity.getId());
            sentenciaPreparada.setString(2,entity.getNombre());

            sentenciaPreparada.executeUpdate();
        } catch (SQLException exception) {
            var mensajeTecnico = "Se presentó una SQLException tratando de registrar la nueva informacion de EstadoReserva en la base de datos, para más detalles revise el log de errores";
            var mensajeUsuario = "Se ha presentado un problema tratando de registrar la nueva informacion de EstadoReserva en la fuente de datos";

            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);

        }catch (Exception exception) {
            var mensajeTecnico = "Se presentó una excepción NO CONTROLADA tratando de ingresar la nueva infromacion de EstadoReserva en la base de datos, para más detalles revise el log de errores";
            var mensajeUsuario = "Se ha presentado un problema inesperado tratando de ingresar la nueva informacion de EstadoReserva en la base de datos    ";

            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        }
    }

    @Override
    public void eliminar(UUID id) throws BackEndException {
        var sentenciaSQL = new StringBuilder();
        sentenciaSQL.append("DELETE FROM EstadoReserva WHERE id = ?)");
        try (var sentenciaPreparada = connection.prepareStatement(sentenciaSQL.toString())){
            sentenciaPreparada.setObject(1,id);

            sentenciaPreparada.executeUpdate();
        } catch (SQLException exception) {
            var mensajeTecnico = "Se presentó una SQLException tratando de hacer un DELETE en la tabla cancha de EstadoReserva en la base de datos, para más detalles revise el log de errores";
            var mensajeUsuario = "Se ha presentado un problema tratando de eliminar definitivamente informacion de EstadoReserva deseada de la fuente de datos";

            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);

        }catch (Exception exception) {
            var mensajeTecnico = "Se presentó una excepción NO CONTROLADA tratando de hacer un DELETE en la tabla EstadoReserva en la base de datos, para más detalles revise el log de errores";
            var mensajeUsuario = "Se ha presentado un problema INESPERADO tratando de borrar definitivamente la informacion de EstadoReserva en la base de datos    ";

            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        }
    }
    @Override
    public List<EstadoReservaEntity> consultar(EstadoReservaEntity entity) {
        return List.of();
    }

    @Override
    public EstadoReservaEntity consultarPorId(UUID id) throws BackEndException {
        var sql = new StringBuilder();
        sql.append("SELECT codigoestadores, nombre ")
                .append("FROM doodb.estadoreserva ")
                .append("WHERE codigoestadores = ?");

        var estadoEntity = new EstadoReservaEntity();

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // 1) Obtener el UUID
                    UUID estadoUUID = UtilUUID.convertirAUUID(rs.getString("codigoestadores"));
                    estadoEntity.setId(estadoUUID);

                    // 2) Obtener el nombre
                    String nombre = rs.getString("nombre");
                    estadoEntity.setNombre(nombre);
                }
            }
        } catch (SQLException exception) {
            var mensajeTecnico   = "Se presentó una SQLException tratando de consultar el estado de reserva por ID en la base de datos.";
            var mensajeUsuario   = "No fue posible consultar el estado de reserva en este momento.";
            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        } catch (Exception exception) {
            var mensajeTecnico   = "Se presentó una excepción NO CONTROLADA tratando de consultar el estado de reserva por ID.";
            var mensajeUsuario   = "Ha ocurrido un problema inesperado al consultar el estado de reserva.";
            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        }

        return estadoEntity;
    }


    @Override
    public void modificar(UUID id, EstadoReservaEntity entity) throws BackEndException{
        var sentenciaSQL = new StringBuilder();
        sentenciaSQL.append("UPDATE cancha SET nombre = ? WHERE id = ?)");
        try (var sentenciaPreparada = connection.prepareStatement(sentenciaSQL.toString())){
            sentenciaPreparada.setObject(1, id);
            sentenciaPreparada.setString(2,entity.getNombre());


            sentenciaPreparada.executeUpdate();
        } catch (SQLException exception) {
            var mensajeTecnico = "Se presentó una SQLException tratando de modificar la nueva informacion de EstadoReserva deseada en la base de datos, para más detalles revise el log de errores";
            var mensajeUsuario = "Se ha presentado un problema tratando de modificar la nueva informacion de EstadoReserva deseada en la fuente de datos";

            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);

        }catch (Exception exception) {
            var mensajeTecnico = "Se presentó una excepción NO CONTROLADA tratando de hacer un UPDATE de EstadoReserva en la base de datos, para más detalles revise el log de errores";
            var mensajeUsuario = "Se ha presentado un problema inesperado tratando de modificar la nueva informacion de EstadoReserva deseada en la base de datos    ";

            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        }
    }
}
