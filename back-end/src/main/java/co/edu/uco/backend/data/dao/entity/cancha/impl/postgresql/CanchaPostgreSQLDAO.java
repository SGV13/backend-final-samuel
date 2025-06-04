package co.edu.uco.backend.data.dao.entity.cancha.impl.postgresql;
import co.edu.uco.backend.crosscutting.exceptions.BackEndException;
import co.edu.uco.backend.crosscutting.exceptions.DataBackEndException;
import co.edu.uco.backend.crosscutting.utilitarios.UtilUUID;
import co.edu.uco.backend.data.dao.entity.cancha.CanchaDAO;
import co.edu.uco.backend.entity.CanchaEntity;
import co.edu.uco.backend.entity.OrganizacionDeportivaEntity;
import co.edu.uco.backend.entity.SuperficieEntity;
import co.edu.uco.backend.entity.TipoCanchaEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class CanchaPostgreSQLDAO implements CanchaDAO {

    private final Connection connection;

    public CanchaPostgreSQLDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void eliminar(UUID codigocancha) throws BackEndException{
        var sentenciaSQL = new StringBuilder();
        sentenciaSQL.append("DELETE FROM cancha WHERE codigocancha = ?)");
        try (var sentenciaPreparada = connection.prepareStatement(sentenciaSQL.toString())){
            sentenciaPreparada.setObject(1,codigocancha);

            sentenciaPreparada.executeUpdate();
        } catch (SQLException exception) {
            var mensajeTecnico = "Se presentó una SQLException tratando de hacer un DELETE en la tabla de la cancha en la base de datos, para más detalles revise el log de errores";
            var mensajeUsuario = "Se ha presentado un problema tratando de eliminar definitivamente informacion de la cancha deseada de la fuente de datos";

            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);

        }catch (Exception exception) {
            var mensajeTecnico = "Se presentó una excepción NO CONTROLADA tratando de hacer un DELETE en la tabla cancha en la base de datos, para más detalles revise el log de errores";
            var mensajeUsuario = "Se ha presentado un problema INESPERADO tratando de borrar definitivamente la informacion de la cancha en la base de datos    ";

            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        }
    }






    @Override
    public List<CanchaEntity> consultar(CanchaEntity filtro) {
        return List.of();
    }

    @Override
    public CanchaEntity consultarPorId(UUID id) throws BackEndException {

        var canchaRetorno = new CanchaEntity();
        var sql = new StringBuilder();

        // Corregimos la lista de columnas según el esquema:
        sql.append(
                "SELECT "
                        +   "codigocancha, "
                        +   "nombre, "
                        +   "costoporhora, "
                        +   "iluminacion, "
                        +   "cubierta, "
                        +   "codigotipocancha, "
                        +   "superficieid, "
                        +   "codigoorganizacion "
                        + "FROM doodb.cancha "
                        + "WHERE codigocancha = ?"
        );

        try (var ps = connection.prepareStatement(sql.toString())) {
            ps.setObject(1, id);

            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    // 1) ID de la cancha
                    UUID canchaUUID = UtilUUID.convertirAUUID(rs.getString("codigocancha"));
                    canchaRetorno.setId(canchaUUID);

                    // 2) Nombre
                    String nombre = rs.getString("nombre");
                    canchaRetorno.setNombreCancha(nombre);

                    // 3) Costo por hora
                    double costoHora = rs.getDouble("costoporhora");
                    canchaRetorno.setCostoHora(costoHora);

                    // 4) Iluminación
                    boolean iluminacion = rs.getBoolean("iluminacion");
                    canchaRetorno.setIluminacion(iluminacion);

                    // 5) Cubierta
                    boolean cubierta = rs.getBoolean("cubierta");
                    canchaRetorno.setCubierta(cubierta);

                    // 6) FK: codigotipocancha (solo asignamos el ID, si quieres cargar la entidad completa haz otro SELECT)
                    String tipoIdStr = rs.getString("codigotipocancha");
                    if (tipoIdStr != null) {
                        UUID tipoUUID = UtilUUID.convertirAUUID(tipoIdStr);
                        TipoCanchaEntity tipo = new TipoCanchaEntity();
                        tipo.setId(tipoUUID);
                        canchaRetorno.setTipo(tipo);
                    } else {
                        canchaRetorno.setTipo(null);
                    }

                    // 7) FK: superficieid
                    String superficieIdStr = rs.getString("superficieid");
                    if (superficieIdStr != null) {
                        UUID superficieUUID = UtilUUID.convertirAUUID(superficieIdStr);
                        SuperficieEntity superficie = new SuperficieEntity();
                        superficie.setId(superficieUUID);
                        canchaRetorno.setSuperficie(superficie);
                    } else {
                        canchaRetorno.setSuperficie(null);
                    }

                    // 8) FK: codigoorganizacion
                    String orgIdStr = rs.getString("codigoorganizacion");
                    if (orgIdStr != null) {
                        UUID orgUUID = UtilUUID.convertirAUUID(orgIdStr);
                        OrganizacionDeportivaEntity org = new OrganizacionDeportivaEntity();
                        org.setId(orgUUID);
                        canchaRetorno.setOrganizacion(org);
                    } else {
                        canchaRetorno.setOrganizacion(null);
                    }
                }
            }
        } catch (SQLException exception) {
            var mensajeTecnico = "Se presentó una SQLException tratando de consultar la información de la cancha con el ID deseado. Para más detalles, revise el log de errores.";
            var mensajeUsuario = "No se pudo consultar la información de la cancha en este momento.";
            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        } catch (Exception exception) {
            var mensajeTecnico = "Excepción NO CONTROLADA al consultar la información de la cancha con el ID deseado.";
            var mensajeUsuario = "Ha ocurrido un problema inesperado al consultar la información de la cancha.";
            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        }

        return canchaRetorno;
    }

    @Override
    public void crear(CanchaEntity entity) throws BackEndException {
        var sentenciaSQL = new StringBuilder();
        sentenciaSQL.append("INSERT INTO cancha(codigocancha, nombreCancha, tipo, dimensiones, superficie, costoHora, ubicacion, organizacion, iluminacion, cubierta, HorariosDisponibles, HorariosEspeciales)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        try (var sentenciaPreparada = connection.prepareStatement(sentenciaSQL.toString())){
            sentenciaPreparada.setObject(1,entity.getId());
            sentenciaPreparada.setString(2,entity.getNombreCancha());
            sentenciaPreparada.setObject(3,entity.getTipo());
            sentenciaPreparada.setObject(4,entity.getSuperficie());
            sentenciaPreparada.setObject(5,entity.getDimensiones());
            sentenciaPreparada.setDouble(6,entity.getCostoHora());
            sentenciaPreparada.setObject(7,entity.getUbicacion());
            sentenciaPreparada.setObject(8,entity.getOrganizacion());
            sentenciaPreparada.setBoolean(9,entity.isIluminacion());
            sentenciaPreparada.setBoolean(10,entity.isCubierta());
            sentenciaPreparada.setObject(11,entity.getHorariosDisponibles());
            sentenciaPreparada.setObject(12,entity.getHorariosEspeciales());

            sentenciaPreparada.executeUpdate();
        } catch (SQLException exception) {
        var mensajeTecnico = "Se presentó una SQLException tratando de registrar la nueva informacion de la cancha en la base de datos, para más detalles revise el log de errores";
        var mensajeUsuario = "Se ha presentado un problema tratando de registrar la nueva informacion de la cancha en la fuente de datos";

        throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);

        }catch (Exception exception) {
        var mensajeTecnico = "Se presentó una excepción NO CONTROLADA tratando de ingresar la nueva infromacion de la cancha en la base de datos, para más detalles revise el log de errores";
        var mensajeUsuario = "Se ha presentado un problema inesperado tratando de ingresar la nueva informacion de la cancha en la base de datos    ";

        throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        }
    }

    @Override
    public void modificar(UUID codigocancha, CanchaEntity entity) throws BackEndException{
        var sentenciaSQL = new StringBuilder();
        sentenciaSQL.append("UPDATE cancha SET nombreCancha = ?, tipo = ?, dimensiones = ?, superficie = ?, costoHora = ?, ubicacion = ?, iluminacion = ?, cubierta = ?, HorariosDisponibles = ?, HorariosEspeciales = ? WHERE codigocancha = ?)");
        try (var sentenciaPreparada = connection.prepareStatement(sentenciaSQL.toString())){
            sentenciaPreparada.setObject(1,codigocancha);
            sentenciaPreparada.setString(2,entity.getNombreCancha());
            sentenciaPreparada.setObject(3,entity.getTipo());
            sentenciaPreparada.setObject(4,entity.getSuperficie());
            sentenciaPreparada.setObject(5,entity.getDimensiones());
            sentenciaPreparada.setDouble(6,entity.getCostoHora());
            sentenciaPreparada.setObject(7,entity.getUbicacion());
            sentenciaPreparada.setObject(8,entity.getOrganizacion());
            sentenciaPreparada.setBoolean(9,entity.isIluminacion());
            sentenciaPreparada.setBoolean(10,entity.isCubierta());
            sentenciaPreparada.setObject(11,entity.getHorariosDisponibles());
            sentenciaPreparada.setObject(12,entity.getHorariosEspeciales());

            sentenciaPreparada.executeUpdate();
        } catch (SQLException exception) {
            var mensajeTecnico = "Se presentó una SQLException tratando de modificar la nueva informacion de la cancha deseada en la base de datos, para más detalles revise el log de errores";
            var mensajeUsuario = "Se ha presentado un problema tratando de modificar la nueva informacion de la cancha deseada en la fuente de datos";

            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);

        }catch (Exception exception) {
            var mensajeTecnico = "Se presentó una excepción NO CONTROLADA tratando de hacer un UPDATE de la cancha deseada en la base de datos, para más detalles revise el log de errores";
            var mensajeUsuario = "Se ha presentado un problema inesperado tratando de modificar la nueva informacion de la cancha deseada en la base de datos    ";

            throw DataBackEndException.reportar(mensajeUsuario, mensajeTecnico, exception);
        }
    }
}





