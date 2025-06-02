package co.edu.uco.backend.data.dao.entity.reserva.impl.postgresql;

import co.edu.uco.backend.crosscutting.exceptions.BackEndException;
import co.edu.uco.backend.crosscutting.exceptions.DataBackEndException;
import co.edu.uco.backend.crosscutting.utilitarios.UtilUUID;
import co.edu.uco.backend.data.dao.entity.reserva.ReservaDAO;
import co.edu.uco.backend.entity.CanchaEntity;
import co.edu.uco.backend.entity.ClienteEntity;
import co.edu.uco.backend.entity.EstadoReservaEntity;
import co.edu.uco.backend.entity.ReservaEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReservaPostgreSQLDAO implements ReservaDAO {

    private final Connection connection;

    public ReservaPostgreSQLDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void crear(ReservaEntity entity) throws BackEndException {
        var sentenciaSQL = new StringBuilder();
        sentenciaSQL.append("INSERT INTO reserva(codigoreserva,codigocliente, codigocancha, fechareserva, fechausocancha, horainicio, horafin, codigoestadores)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        );
        try (var sentenciaPreparada = connection.prepareStatement(sentenciaSQL.toString())){
            sentenciaPreparada.setObject(1,entity.getId());
            sentenciaPreparada.setObject(2,entity.getCliente().getId());
            sentenciaPreparada.setObject(3,entity.getCancha().getId());
            sentenciaPreparada.setObject(4,entity.getFechaReserva());
            sentenciaPreparada.setObject(5,entity.getFechaUsoCancha());
            sentenciaPreparada.setObject(6,entity.getHoraInicio());
            sentenciaPreparada.setObject(7,entity.getHoraFin());
            sentenciaPreparada.setObject(8,entity.getEstado().getId());


            sentenciaPreparada.executeUpdate();
        } catch (SQLException exception) {
            var mensajeTecnico = "Se presentó una SQLException tratando de registrar la nueva informacion de la reserva en la base de datos, para más detalles revise el log de errores";
            var mensajeUsuario = "Se ha presentado un problema tratando de registrar la nueva informacion de la reserva en la fuente de datos";

            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);

        }catch (Exception exception) {
            var mensajeTecnico = "Se presentó una excepción NO CONTROLADA tratando de ingresar la nueva infromacion de la reserva en la base de datos, para más detalles revise el log de errores";
            var mensajeUsuario = "Se ha presentado un problema inesperado tratando de ingresar la nueva informacion de la reserva en la base de datos    ";

            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        }
    }

    @Override
    public void eliminar(UUID reservaId) throws BackEndException {
        var sentenciaSQL = new StringBuilder();
        sentenciaSQL.append("DELETE FROM reserva WHERE codigoreserva = ?");
        try (var sentenciaPreparada = connection.prepareStatement(sentenciaSQL.toString())){
            sentenciaPreparada.setObject(1,reservaId);

            sentenciaPreparada.executeUpdate();
        } catch (SQLException exception) {
            var mensajeTecnico = "Se presentó una SQLException tratando de hacer un DELETE en la tabla reserva en la base de datos, para más detalles revise el log de errores";
            var mensajeUsuario = "Se ha presentado un problema tratando de eliminar definitivamente informacion de la reserva deseada de la fuente de datos";

            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);

        }catch (Exception exception) {
            var mensajeTecnico = "Se presentó una excepción NO CONTROLADA tratando de hacer un DELETE en la tabla reserva en la base de datos, para más detalles revise el log de errores";
            var mensajeUsuario = "Se ha presentado un problema INESPERADO tratando de borrar definitivamente la informacion de la reserva en la base de datos    ";

            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        }
    }

    @Override
    public List<ReservaEntity> consultar(ReservaEntity entity) {
        return List.of();
    }

    @Override
    public ReservaEntity consultarPorId(UUID id) throws BackEndException {
        // Creamos una ReservaEntity "vacía" que rellenaremos si existe la fila
        var reservaEntityRetorno = new ReservaEntity();

        // Construimos la sentencia SQL
        var sql = new StringBuilder();
        sql.append(
                "SELECT "
                        +   "codigoreserva, codigocliente, codigocancha, "
                        +   "fechareserva, fechausocancha, horainicio, horafin, codigoestadores "
                        + "FROM doodb.reserva "
                        + "WHERE codigoreserva = ?"
        );

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            // 1) Asignar el parámetro del WHERE
            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // 2) ID de la reserva
                    UUID reservaUUID = UtilUUID.convertirAUUID(rs.getString("codigoreserva"));
                    reservaEntityRetorno.setId(reservaUUID);

                    // 3) Cliente (solo su ID)
                    UUID clienteUUID = UtilUUID.convertirAUUID(rs.getString("codigocliente"));
                    ClienteEntity cliente = new ClienteEntity();
                    cliente.setId(clienteUUID);
                    reservaEntityRetorno.setCliente(cliente);

                    // 4) Cancha (solo su ID)
                    UUID canchaUUID = UtilUUID.convertirAUUID(rs.getString("codigocancha"));
                    CanchaEntity cancha = new CanchaEntity();
                    cancha.setId(canchaUUID);
                    reservaEntityRetorno.setCancha(cancha);

                    // 5) fechaReserva (DATE)
                    LocalDate fechaRes = rs.getObject("fechareserva", LocalDate.class);
                    reservaEntityRetorno.setFechaReserva(fechaRes);

                    // 6) fechaUsoCancha (DATE)
                    LocalDate fechaUso = rs.getObject("fechausocancha", LocalDate.class);
                    reservaEntityRetorno.setFechaUsoCancha(fechaUso);

                    // 7) horaInicio (TIME)
                    LocalTime hIni = rs.getObject("horainicio", LocalTime.class);
                    reservaEntityRetorno.setHoraInicio(hIni);

                    // 8) horaFin (TIME)
                    LocalTime hFin = rs.getObject("horafin", LocalTime.class);
                    reservaEntityRetorno.setHoraFin(hFin);

                    // 9) estadoReserva (se almacena como UUID en la columna codigoestadores)
                    UUID estadoUUID = UtilUUID.convertirAUUID(rs.getString("codigoestadores"));
                    EstadoReservaEntity estado = new EstadoReservaEntity();
                    estado.setId(estadoUUID);
                    reservaEntityRetorno.setEstado(estado);
                }
            }
        } catch (SQLException exception) {
            var mensajeTecnico = "Se presentó una SQLException intentando consultar la reserva por ID";
            var mensajeUsuario = "No se pudo consultar la información de la reserva en este momento.";
            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        } catch (Exception exception) {
            var mensajeTecnico = "Excepción NO CONTROLADA al consultar la reserva por ID";
            var mensajeUsuario = "Ha ocurrido un problema inesperado al consultar la reserva.";
            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        }

        return reservaEntityRetorno;
    }


    @Override
    public void modificar(UUID reservaId, ReservaEntity entity) throws BackEndException {
        var sentenciaSQL = new StringBuilder();
        sentenciaSQL.append("UPDATE reserva SET codigocliente = ?, codigocancha = ?, fechareserva = ?, fechausocancha = ?, horainicio = ?, horafin = ?, codigoestadores = ? WHERE codigoreserva = ?");
        try (var sentenciaPreparada = connection.prepareStatement(sentenciaSQL.toString())){
            sentenciaPreparada.setObject(1,entity.getCliente().getId());
            sentenciaPreparada.setObject(2,entity.getCancha().getId());
            sentenciaPreparada.setObject(3,entity.getFechaReserva());
            sentenciaPreparada.setObject(4,entity.getFechaUsoCancha());
            sentenciaPreparada.setObject(5,entity.getHoraInicio());
            sentenciaPreparada.setObject(6,entity.getHoraFin());
            sentenciaPreparada.setObject(7,entity.getEstado());
            sentenciaPreparada.setObject(8,reservaId);

            sentenciaPreparada.executeUpdate();
        } catch (SQLException exception) {
            var mensajeTecnico = "Se presentó una SQLException tratando de modificar la nueva informacion de la reserva deseada en la base de datos, para más detalles revise el log de errores";
            var mensajeUsuario = "Se ha presentado un problema tratando de modificar la nueva informacion de la reserva deseada en la fuente de datos";

            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);

        }catch (Exception exception) {
            var mensajeTecnico = "Se presentó una excepción NO CONTROLADA tratando de hacer un UPDATE de la reserva deseada en la base de datos, para más detalles revise el log de errores";
            var mensajeUsuario = "Se ha presentado un problema inesperado tratando de modificar la nueva informacion de la reserva deseada en la base de datos    ";

            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        }

    }

    @Override
    public List<ReservaEntity> consultarPorCliente(UUID clienteId) throws BackEndException {
        var listaReservas = new ArrayList<ReservaEntity>();

        var sql = new StringBuilder();
        sql.append(
                "SELECT "
                        +   "codigoreserva, codigocliente, codigocancha, "
                        +   "fechareserva, fechausocancha, horainicio, horafin, codigoestadores "
                        + "FROM doodb.reserva "
                        + "WHERE codigocliente = ?"
        );

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            // 1) Asignar el parámetro del WHERE
            ps.setObject(1, clienteId);

            try (ResultSet rs = ps.executeQuery()) {
                // 2) Para cada fila, creamos un objeto ReservaEntity y lo poblamos
                while (rs.next()) {
                    var entity = new ReservaEntity();

                    // a) ID de la reserva
                    UUID reservaUUID = UtilUUID.convertirAUUID(rs.getString("codigoreserva"));
                    entity.setId(reservaUUID);

                    // b) Cliente
                    UUID clienteUUID = UtilUUID.convertirAUUID(rs.getString("codigocliente"));
                    ClienteEntity cliente = new ClienteEntity();
                    cliente.setId(clienteUUID);
                    entity.setCliente(cliente);

                    // c) Cancha
                    UUID canchaUUID = UtilUUID.convertirAUUID(rs.getString("codigocancha"));
                    CanchaEntity cancha = new CanchaEntity();
                    cancha.setId(canchaUUID);
                    entity.setCancha(cancha);

                    // d) fechaReserva
                    LocalDate fechaRes = rs.getObject("fechareserva", LocalDate.class);
                    entity.setFechaReserva(fechaRes);

                    // e) fechaUsoCancha
                    LocalDate fechaUso = rs.getObject("fechausocancha", LocalDate.class);
                    entity.setFechaUsoCancha(fechaUso);

                    // f) horaInicio
                    LocalTime hIni = rs.getObject("horainicio", LocalTime.class);
                    entity.setHoraInicio(hIni);

                    // g) horaFin
                    LocalTime hFin = rs.getObject("horafin", LocalTime.class);
                    entity.setHoraFin(hFin);

                    // h) estadoReserva
                    UUID estadoUUID = UtilUUID.convertirAUUID(rs.getString("codigoestadores"));
                    EstadoReservaEntity estado = new EstadoReservaEntity();
                    estado.setId(estadoUUID);
                    entity.setEstado(estado);

                    // i) Agregar esta entidad a la lista
                    listaReservas.add(entity);
                }
            }
        } catch (SQLException exception) {
            var mensajeTecnico = "Se presentó una SQLException intentando listar reservas por cliente";
            var mensajeUsuario = "No se pudo obtener las reservas en este momento.";
            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        } catch (Exception exception) {
            var mensajeTecnico = "Excepción NO CONTROLADA al listar reservas por cliente";
            var mensajeUsuario = "Ha ocurrido un problema inesperado al obtener las reservas.";
            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        }

        return listaReservas;
    }
}
