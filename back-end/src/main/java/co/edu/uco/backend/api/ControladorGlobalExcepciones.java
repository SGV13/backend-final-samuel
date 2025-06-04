package co.edu.uco.backend.api;

import co.edu.uco.backend.crosscutting.exceptions.BackEndException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControladorGlobalExcepciones {

    @ExceptionHandler(BackEndException.class)
    public ResponseEntity<String> controlarBackendException(BackEndException exception) {
        exception.printStackTrace();
        return new ResponseEntity<>(exception.getMensajeUsuario(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> controlarFormatoEntero(HttpMessageNotReadableException ex) {
        Throwable root = ex.getMostSpecificCause();
        if (root instanceof MismatchedInputException) {
            MismatchedInputException mie = (MismatchedInputException) root;
            boolean esCalificacion = mie.getPath().stream()
                    .anyMatch(ref -> "calificacion".equals(ref.getFieldName()));
            if (esCalificacion) {
                return new ResponseEntity<>(
                        "Error de formato: La calificación solo permite números enteros (1–5), sin decimales ni texto.",
                        HttpStatus.BAD_REQUEST
                );
            }
        }
        // Si no es por 'calificacion', delegamos al handler genérico de Exception:
        return new ResponseEntity<>(
                "JSON inválido: " + ex.getLocalizedMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> controlarException(Exception exception) {
        exception.printStackTrace();
        return new ResponseEntity<>("Se ha presentado un problema tratando de llevar a cabo la operación deseada", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
