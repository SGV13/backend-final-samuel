package co.edu.uco.backend.businesslogic.businesslogic.impl;

import co.edu.uco.backend.businesslogic.assembler.resena.entity.ResenaEntityAssembler;
import co.edu.uco.backend.businesslogic.assembler.reserva.entity.ReservaEntityAssembler;
import co.edu.uco.backend.businesslogic.businesslogic.ResenaBusinessLogic;
import co.edu.uco.backend.businesslogic.businesslogic.domain.ResenaDomain;
import co.edu.uco.backend.businesslogic.businesslogic.domain.ReservaDomain;
import co.edu.uco.backend.crosscutting.exceptions.BackEndException;
import co.edu.uco.backend.crosscutting.exceptions.BusinessLogicBackEndException;
import co.edu.uco.backend.crosscutting.utilitarios.UtilEntero;
import co.edu.uco.backend.crosscutting.utilitarios.UtilTexto;
import co.edu.uco.backend.crosscutting.utilitarios.UtilUUID;
import co.edu.uco.backend.data.dao.factory.DAOFactory;
import co.edu.uco.backend.entity.EstadoReservaEntity;
import co.edu.uco.backend.entity.ResenaEntity;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class ResenaBusinessLogicImpl implements ResenaBusinessLogic {

    private final DAOFactory factory;
    public ResenaBusinessLogicImpl(DAOFactory factory) {
        this.factory = factory;
    }

    @Override
    public void registrarNuevaResena(UUID reservaId, ResenaDomain resena) throws BackEndException {

        //  1. Verificar que la reserva a la que se asociará esta reseña efectivamente exista
        //  Reseña-POL-negocio. Un cliente solo podrá reseñar cuando haya reservado esta cancha previamente, y el estado de la reserva sea "completada"
        ReservaDomain reservaDomain = obtenerReservaDomain(reservaId);
        List<ResenaEntity> existentes = factory.getResenaDAO().consultarPorReserva(reservaId);
        if (!existentes.isEmpty()) {
            // Ya hay al menos una
            throw BusinessLogicBackEndException.reportar(
                    "Ya existe una reseña para la reserva " + reservaId
            );
        }
        //  2. Calcular la fecha definitiva: si resena.getFecha() es null, usamos LocalDate.now()
        LocalDate fechaValida = configurarFechaALaActual(resena.getFecha());
        //  3. Reseña-POL-0004. Asegurar que los datos requeridos para llevar a cabo la acción sean válidos a nivel de tipo de dato, longitud, obligatoriedad, formato y rango.
        validarIntegridadInformacionRegistrarNuevaResena(resena);
        validarIntegridadFecha(fechaValida);
        //  4. Reseña-POL-0001. Si el cliente ingresa una puntuación menor o igual a 2, el cuerpo del comentario debe tener al menos 50 caracteres
        //  y mencionar al menos un aspecto negativo concreto (por ejemplo, incluir palabras como “mal”, “inadecuado”, “deficiente”, etc.).
        validacionPoliticaNro1(resena);
        //  5. Reseña-POL-0002. El campo comentario no puede contener URLs (p. ej. “http://” o “www.”) ni etiquetas HTML (p. ej. <a>, <script>, <img>, etc.).
        validacionPoliticaNro2(resena);
        //  6. Reseña-POL-0003. El comentario no puede contener palabras ofensivas (una lista predefinida de “palabras prohibidas#”, p. ej. insultos o groserías).
        validacionPoliticaNro3(resena);

        //  7. Generar UUID y recrear domain
        UUID id = generarIdentificadorNuevaResena();
        ResenaDomain toCreate = new ResenaDomain(
                id,
                reservaDomain,
                resena.getCalificacion(),
                resena.getComentario(),
                fechaValida
        );
        //  8. Registrar la nueva reseña siempre y cuando se cumplan todas las políticas
        ResenaEntity resenaEntity = ResenaEntityAssembler.getInstance().toEntity(toCreate);
        factory.getResenaDAO().crear(resenaEntity);
    }

    @Override
    public void modificarResenaExistente(UUID reservaId, UUID resenaId, ResenaDomain resena) throws BackEndException {
        //  1. Verificar nuevamente que la reserva asociada a la reseña efectivamente exista
        ReservaDomain reservaDomain = obtenerReservaDomain(reservaId);
        //  2. Verificar que el UUID de resenaId proporcionado realmente exista en la base de datos
        ResenaDomain existente = cargarResenaExistente(resenaId);
        if (!existente.getReserva().getId().equals(reservaId)) {
            throw BusinessLogicBackEndException.reportar(
                    "La reseña con id " + resenaId + " no pertenece a la reserva con id " + reservaId
            );
        }
        //  3. “Fecha automática” — si el DTO no trae fecha (es null), se usa LocalDate.now()
        LocalDate fechaValida = configurarFechaALaActual(resena.getFecha());
        //  4. Reseña-POL-0005. En caso de cambiar solo algunos campos, los otros deben permanecer iguales
        int nuevaCalificacion = (resena.getCalificacion() != 0)
                ? resena.getCalificacion()
                : existente.getCalificacion();
        String nuevoComentario = UtilTexto.getInstance().estaVacia(resena.getComentario())
                ? existente.getComentario()
                : resena.getComentario().trim();

        ResenaDomain merged = new ResenaDomain(
                resenaId,
                reservaDomain,
                nuevaCalificacion,
                nuevoComentario,
                fechaValida
        );

        //  5. Integridad de datos
        validarIntegridadInformacionRegistrarNuevaResena(merged);
        validarIntegridadFecha(fechaValida);


        //  6. Reseña-POL-0006. Incluye lo mismo de las politicas 0001,0002,0003 y la politica de negocio
        validacionPoliticaNro1(merged); // si ≤ 2, longitud ≥ 50 y debe mencionar aspecto negativo
        validacionPoliticaNro2(merged); // no URLs ni etiquetas HTML
        validacionPoliticaNro3(merged); // no palabras ofensivas

        //  7. Ejecutar update siempre y cuando se cumplan todas las politicas
        ResenaEntity resenaEntity = ResenaEntityAssembler.getInstance().toEntity(merged);
        factory.getResenaDAO().modificar(resenaId, resenaEntity);
    }

    @Override
    public void darBajaDefinitivamenteResenaExistente(UUID reservaId, UUID resenaId) throws BackEndException {
        // 1) Traer entity y validar su existencia
        ResenaDomain existente = cargarResenaExistente(resenaId);
        // 2) Verificar que la reseña que vino pertenezca a la reservaId
        if (!existente.getReserva().getId().equals(reservaId)) {
            throw BusinessLogicBackEndException.reportar(
                    "La reseña " + resenaId + " no pertenece a la reserva " + reservaId
            );
        }
        // 3) Borrado definitivo
        factory.getResenaDAO().eliminar(resenaId);
    }

    @Override
    public ResenaDomain consultarResenaPorReserva(UUID reservaId, UUID resenaId) throws BackEndException {
        // 1) Traer la reseña por su propio ID
        var entity = factory.getResenaDAO().consultarPorId(resenaId);
        if (UtilUUID.esValorDefecto(entity.getId())) {
            throw BusinessLogicBackEndException.reportar("No existe la reseña con id: " + resenaId);
        }

        // 2) Verificar que la reseña que vino realmente esté asociada a la reservaId solicitado
        UUID reservaQueTrajo = entity.getReserva().getId();
        if (!reservaQueTrajo.equals(reservaId)) {
            throw BusinessLogicBackEndException.reportar(
                    "La reseña " + resenaId + " no pertenece a la reserva " + reservaId
            );
        }

        // 3) Convertir a domain y devolver
        return ResenaEntityAssembler.getInstance().toDomain(entity);
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
    }

    private void validarIntegridadCalificacion(int calificacion) throws BackEndException {

        // Convertimos el entero a string (por ejemplo, 2 → "2"; -3 → "-3"; 0 → "0")
        String calStr = UtilEntero.getInstance().convertirAString(calificacion);
        // Si esa cadena NO es solo dígitos (p.ej. "-3" o "2.5" o "abc"), la rechazamos
        if (!UtilTexto.getInstance().contieneSoloNumeros(calStr)) {
            throw BusinessLogicBackEndException.reportar(
                    "Error de formato: La calificación solo puede contener números enteros (sin decimales ni texto). Se recibió: "
                            + calStr
            );
        }
        // 1. Obligatorio: la calificación debe estar presente
        if (calificacion == 0) {
            throw BusinessLogicBackEndException.reportar(
                    "Error de obligatoriedad: La calificación es un dato obligatorio.");
        }
        // 2. Longitud: en este caso, un dígito (entre 1 y 5)
        String val = String.valueOf(calificacion);
        if (val.length() > 1) {
            throw BusinessLogicBackEndException.reportar(
                    "Error de longitud: La calificación debe tener sólo un carácter. Se ingresó: " + val.length());
        }
        // 3. Formato: solo números enteros, si se ingresan letras, decimales o caracteres especiales se debe mostrar excepción
        // 4. Rango: entre 1 y 5
        if (calificacion < 1 || calificacion > 5) {
            throw BusinessLogicBackEndException.reportar(
                    "Error de rango: La calificación debe estar entre 1 y 5. Se ingresó: " + calificacion);
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

    private void validarIntegridadFecha(LocalDate fecha) throws BackEndException {
        // 1. Obligatoriedad: si es null, la BL lo completará con LocalDate.now(), así que no lo tratamos aquí.
        if (fecha == null) {
            return;
        }
        // 2. Formato y longitud: Ya se valida antes
        // 3. Rango: la fecha debe estar entre 01/01/2025 y 31/12/9999
        LocalDate min = LocalDate.of(2025, 1, 1);
        LocalDate max = LocalDate.of(9999, 12, 31);

        if (fecha.isBefore(min) || fecha.isAfter(max)) {
            throw BusinessLogicBackEndException.reportar(
                    "Error de rango: La fecha de la reseña debe estar entre 01/01/2025 y 31/12/9999. Se ingresó: "
                            + fecha
            );
        }
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

    /**
     * POL-REV-001:
     * Si la calificación ≤ 2, el comentario debe tener al menos 50 caracteres
     * y mencionar al menos un aspecto negativo (“mal”, “deficiente”, etc.).
     */
    private void validacionPoliticaNro1(ResenaDomain resena) throws BackEndException {
        int calificacion = resena.getCalificacion();
        if (calificacion <= 2) {
            String comentario = resena.getComentario().trim();

            // a) longitud mínima 50
            if (comentario.length() < 50) {
                throw BusinessLogicBackEndException.reportar(
                        "POL-REV-001: Con calificación ≤ 2, el comentario debe tener al menos 50 caracteres."
                );
            }

            // b) al menos una palabra negativa (normalizando tildes)
            String textoSinAcentos = Normalizer
                    .normalize(comentario.toLowerCase(), Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "");

            List<String> negativos = List.of(
                    "mal", "deficiente", "sucio", "inadecuado", "cancelado",
                    "pesimo", "horrible", "desastroso", "negligente", "incompetente",
                    "lamentable", "pobre", "terrible", "decepcionante", "frustrante"
            );

            boolean contieneAspectoNegativo = negativos.stream()
                    .anyMatch(p -> textoSinAcentos.contains(p));

            if (!contieneAspectoNegativo) {
                throw BusinessLogicBackEndException.reportar(
                        "POL-REV-001: Con calificación ≤ 2, debes mencionar al menos un aspecto negativo "
                                + "(ej.: “mal”, “deficiente”, “pésimo”, “horrible”, etc.)."
                );
            }
        }
    }

    /**
     * POL-REV-002:
     * El comentario no puede contener URLs (“http://”, “https://”, “www.”) ni etiquetas HTML (<a>, <script>, etc.).
     */
    private void validacionPoliticaNro2(ResenaDomain resena) throws BackEndException {
        String comentario = resena.getComentario();
        String textoLower = comentario.toLowerCase();

        // 1) Detectar URLs con protocolo: http:// ó https://
        Pattern urlProtocolPattern = Pattern.compile("(https?://\\S+)");
        if (urlProtocolPattern.matcher(textoLower).find()) {
            throw BusinessLogicBackEndException.reportar(
                    "POL-REV-002: El comentario no puede contener URLs con protocolo (http:// o https://)."
            );
        }

        // 2) Detectar URLs tipo "www."
        Pattern urlWwwPattern = Pattern.compile("(www\\.\\S+)");
        if (urlWwwPattern.matcher(textoLower).find()) {
            throw BusinessLogicBackEndException.reportar(
                    "POL-REV-002: El comentario no puede contener URLs que empiecen con 'www.'."
            );
        }

        // 3) Detectar menciones a dominios sin protocolo: ejemplo.com, ejemplo.net, etc.
        Pattern urlSimplePattern = Pattern.compile("\\b\\w+\\.(com|net|org|info|io|es|co)(/[^\\s]*)?\\b");
        if (urlSimplePattern.matcher(textoLower).find()) {
            throw BusinessLogicBackEndException.reportar(
                    "POL-REV-002: El comentario no puede contener referencias a dominios (.com, .net, .org, etc.)."
            );
        }

        // 4) Bloquear cualquier etiqueta HTML de la forma <…>
        Pattern htmlTagPattern = Pattern.compile("<[^>]+>");
        if (htmlTagPattern.matcher(comentario).find()) {
            throw BusinessLogicBackEndException.reportar(
                    "POL-REV-002: El comentario no puede contener etiquetas HTML (p.ej. <a>, <script>, etc.)."
            );
        }
    }

    /**
     * POL-REV-003:
     * El comentario no puede contener palabras ofensivas (“imbécil”, “estúpido”, “mierda”, etc.).
     */
    private void validacionPoliticaNro3(ResenaDomain resena) throws BackEndException {
        String comentario = resena.getComentario().toLowerCase();

        // Normalizar para quitar tildes/acentos
        String comentarioSinAcentos = Normalizer
                .normalize(comentario, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        // Lista ampliada de palabras prohibidas (todas sin tildes, minúsculas)
        List<String> palabrasProhibidas = new ArrayList<>(Arrays.asList(
                "imbecil", "estupido", "idiota", "basura", "mierda", "puta",
                "gilipollas", "pendejo", "tarado", "cabron", "mierdas",
                "malparido", "malparida", "zorra", "zorras"
        ));

        for (String palabra : palabrasProhibidas) {
            if (comentarioSinAcentos.contains(palabra)) {
                throw BusinessLogicBackEndException.reportar(
                        "POL-REV-003: El comentario contiene palabras ofensivas. Por favor, mantén un lenguaje respetuoso."
                );
            }
        }
    }

    private ReservaDomain obtenerReservaDomain(UUID reservaId) throws BackEndException {
        if (reservaId == null) {
            throw BusinessLogicBackEndException.reportar("El ID de la reserva no puede ser nulo.");
        }

        // 1) Consultar la entidad ReservaEntity
        var reservaEntity = factory.getReservaDAO().consultarPorId(reservaId);
        if (UtilUUID.esValorDefecto(reservaEntity.getId())) {
            throw BusinessLogicBackEndException.reportar(
                    "No existe la reserva con id: " + reservaId
            );
        }

        // 2) Consultar la entidad EstadoReservaEntity completa a partir del estadoId
        UUID estadoId = reservaEntity.getEstado().getId();
        EstadoReservaEntity estadoEntity = factory.getEstadoReservaDAO().consultarPorId(estadoId);
        if (estadoEntity == null || estadoEntity.getId() == null) {
            throw BusinessLogicBackEndException.reportar(
                    "La reserva tiene un estado inválido."
            );
        }

        // 3) Validar que el nombre del estado sea “finalizada” (case-insensitive)
        String nombreEstado = estadoEntity.getNombre(); // ej. “finalizada”, “pendiente”, etc.
        if (!"finalizada".equalsIgnoreCase(nombreEstado)) {
            throw BusinessLogicBackEndException.reportar(
                    "No puedes dejar reseña hasta que la reserva esté finalizada. " +
                            "Estado actual: \"" + nombreEstado + "\"."
            );
        }

        // 4) Transformar la entidad a domain
        return ReservaEntityAssembler.getInstance().toDomain(reservaEntity);

    }

    private LocalDate configurarFechaALaActual(LocalDate fecha) throws BackEndException {
        if (fecha != null) {
            throw BusinessLogicBackEndException.reportar(
                    "No puedes proporcionar la fecha de la reseña; se asigna automáticamente."
            );
        }
        return LocalDate.now();
    }
}
