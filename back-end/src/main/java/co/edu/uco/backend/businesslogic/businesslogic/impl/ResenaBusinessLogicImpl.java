package co.edu.uco.backend.businesslogic.businesslogic.impl;

import co.edu.uco.backend.businesslogic.assembler.resena.entity.ResenaEntityAssembler;
import co.edu.uco.backend.businesslogic.businesslogic.ResenaBusinessLogic;
import co.edu.uco.backend.businesslogic.businesslogic.domain.ResenaDomain;
import co.edu.uco.backend.crosscutting.exceptions.BackEndException;
import co.edu.uco.backend.crosscutting.exceptions.BusinessLogicBackEndException;
import co.edu.uco.backend.crosscutting.utilitarios.UtilEntero;
import co.edu.uco.backend.crosscutting.utilitarios.UtilTexto;
import co.edu.uco.backend.crosscutting.utilitarios.UtilUUID;
import co.edu.uco.backend.data.dao.factory.DAOFactory;
import co.edu.uco.backend.entity.ResenaEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ResenaBusinessLogicImpl implements ResenaBusinessLogic {

    private final DAOFactory factory;
    public ResenaBusinessLogicImpl(DAOFactory factory) {
        this.factory = factory;
    }

    @Override
    public void registrarNuevaResena(UUID reserva, ResenaDomain resena) throws BackEndException {

        //  1. Verificar que la reserva a la que se asociará esta reseña efectivamente exista

        //  2. Reseña-POL-0001. Si el cliente ingresa una puntuación menor o igual a 2, el cuerpo del comentario debe tener al menos 50 caracteres
        //  y mencionar al menos un aspecto negativo concreto (por ejemplo, incluir palabras como “mal”, “inadecuado”, “deficiente”, etc.).

        //  3. Reseña-POL-0002. El campo comentario no puede contener URLs (p. ej. “http://” o “www.”) ni etiquetas HTML (p. ej. <a>, <script>, <img>, etc.).

        //  4. Reseña-POL-0003. El comentario no puede contener palabras ofensivas (una lista predefinida de “palabras prohibidas#”, p. ej. insultos o groserías).

        //  5. Reseña-POL-0004. Asegurar que los datos requeridos para llevar a cabo la acción sean válidos a nivel de tipo de dato, longitud, obligatoriedad, formato y rango.
        validarIntegridadInformacionRegistrarNuevaResena(resena);
        //  6. Configurar fecha a la actual

        //  7. Generar UUID y recrear domain
        UUID id = generarIdentificadorNuevaResena();
        ResenaDomain toCreate = new ResenaDomain(
                id,
                resena.getReserva(),
                resena.getCalificacion(),
                resena.getComentario(),
                resena.getFecha()
        );
        //  8. Registrar la nueva reseña siempre y cuando se cumplan todas las políticas
        ResenaEntity resenaEntity = ResenaEntityAssembler.getInstance().toEntity(toCreate);
        factory.getResenaDAO().crear(resenaEntity);
    }

    @Override
    public void modificarResenaExistente(UUID reservaId, UUID resenaId, ResenaDomain resena) throws BackEndException {
        ResenaEntity resenaEntity = ResenaEntityAssembler.getInstance().toEntity(resena);
        factory.getResenaDAO().modificar(resenaId, resenaEntity);
    }

    @Override
    public void darBajaDefinitivamenteResenaExistente(UUID reservaId, UUID resenaId) throws BackEndException {
        factory.getResenaDAO().eliminar(resenaId);
    }

    @Override
    public ResenaDomain consultarResenaPorReserva(UUID reservaId, UUID resenaId) throws BackEndException {
        factory.getResenaDAO().consultarPorId(resenaId);
        return null;
    }

    @Override
    public List<ResenaDomain> consultarResenas(UUID reservaId, ResenaDomain filtro) throws BackEndException {
        ResenaEntity filterEntity = ResenaEntityAssembler.getInstance().toEntity(filtro);
        List<ResenaEntity> entities = factory.getResenaDAO().consultar(filterEntity);
        return ResenaEntityAssembler.getInstance().toDomain(entities);
    }

    // ———————————————————————————————————————————
    // Métodos privados auxiliares
    // ———————————————————————————————————————————

    private void validarIntegridadInformacionRegistrarNuevaResena(ResenaDomain resena) throws BackEndException {
        validarIntegridadCalificacion(resena.getCalificacion());
        validarIntegridadComentario(resena.getComentario());
        validarIntegridadFecha(resena.getFecha());
    }

    private void validarIntegridadCalificacion(int calificacion) throws BackEndException {
        //  1. obligatoriedad: La calificación en la reseña, debe ser un dato obligatorio
        if (UtilTexto.getInstance().estaVacia(UtilEntero.getInstance().convertirAString(calificacion))) {
            throw BusinessLogicBackEndException.reportar("Error de obligatoriedad, La calificación es un dato obligatorio que debe ingresar");
        }

        //  2. Longitud: Solo puede contener un digito
        String val = UtilTexto.getInstance().quitarEspaciosEnBlancoInicioFin(UtilEntero.getInstance().convertirAString(calificacion));
        if(val.length()>1) {
            throw BusinessLogicBackEndException.reportar("Longitud de la calificación incorrecta, la calificación debe tener solamente un carácter y la ingresada tiene" + val.length());
        }

        //  3. Formato: Solamente debe contener números enteros

        //  4. Rango: Debe cumplir calificación <= 5 y calificación >= 1
        if(calificacion > 5 || calificacion < 1){
            throw BusinessLogicBackEndException.reportar("Rango de la calificación incorrecto, La calificacón debe estar entre 1 y 5, el número ingresado es " + calificacion);
        }
    }

    private void validarIntegridadComentario(String comentario) throws BackEndException {
        //  1. obligatoriedad: El comentario de la reseña es un dato obligatorio
        if (UtilTexto.getInstance().estaVacia(comentario)) {
            throw BusinessLogicBackEndException.reportar("Error de obligatoriedad, el comentario de la reseña es un dato obligatorio");
        }

        //  2. Longitud: Debe tener entre 20 y 500 carácteres
        String val = UtilTexto.getInstance().quitarEspaciosEnBlancoInicioFin(comentario);
        if(val.length() < 20 || val.length() > 500) {
            throw BusinessLogicBackEndException.reportar("Error de longitud, el comentario debe tener entre 20 y 500 carácteres y el ingresado tiene" + val.length());
        }

        //  3.  formato: Puede contener letras, números, espacios y caracteres especiales
        if(!UtilTexto.getInstance().contieneSoloAlfanumericoEspaciosEspeciales(comentario)) {
            throw BusinessLogicBackEndException.reportar("Formato del comentario incorrecto, el comentario solo puede contener letras, números, espacios y carácteres especiales permitidos(@,-,_,.,$...) ");
        }
    }

    private void validarIntegridadFecha(LocalDate fecha) {

        //  1. Obligatoriedad: La fecha de la reseña debe ser un dato obligatorio

        //  2. Longitud: Debe tener 10 caracteres o los que tengan las fechas estándar

        //  3. Formato: dd/mm/aaaa, nota: se debe crear un nuevo utilitario

        //  4. Rango: Debe estar entre  1/01/2025 y 31/12/9999
    }

    private UUID generarIdentificadorNuevaResena() throws BackEndException {
        UUID nuevoId;
        do {
            nuevoId = UtilUUID.generarNuevoUUID();
        } while (!UtilUUID.esValorDefecto(factory.getResenaDAO().consultarPorId(nuevoId).getId()));
        return nuevoId;
    }

    private ResenaDomain cargarResenaExistente(UUID resenaId) throws BackEndException {
        //Para utilizar en consultas, modificaciones y eliminaciones
        var entity = factory.getResenaDAO().consultarPorId(resenaId);
        if (UtilUUID.esValorDefecto(entity.getId())) {
            throw BusinessLogicBackEndException.reportar("No existe la reseña con id: " + resenaId);
        }
        return ResenaEntityAssembler.getInstance().toDomain(entity);
    }

    private void validacionPoliticaNro1(){
        //Aún no sé como implementarlo
    }

    private void validacionPoliticaNro2(){
        //Aún no sé como implementarlo
    }

    private void validacionPoliticaNro3(){
        //Aún no sé como implementarlo
    }

    private void validarReservaExistente(UUID reservaId) throws BackEndException {
        //Aún no sé como implementarlo
    }

    private void configurarFechaALaActual(LocalDate fecha) throws BackEndException {
        //Aún no sé como implementarlo
    }
}
