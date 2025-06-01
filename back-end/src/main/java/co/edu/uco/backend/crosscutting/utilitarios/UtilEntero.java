package co.edu.uco.backend.crosscutting.utilitarios;

public final class UtilEntero {

    private static final UtilEntero instancia = new UtilEntero();
    private static final int VALOR_DEFECTO = 0;

    private UtilEntero() {
        super();
    }

    public static UtilEntero getInstance() {
        return instancia;
    }

    public int obtenerValorDefecto() {
        return VALOR_DEFECTO;
    }

    public int obtenerValorDefecto(final Integer valor) {
        return UtilObjeto.getInstance().obtenerValorDefecto(valor, VALOR_DEFECTO);
    }

    public boolean esPositivo(final Integer valor) {
        return obtenerValorDefecto(valor) > 0;
    }

    public boolean esNegativo(final Integer valor) {
        return obtenerValorDefecto(valor) < 0;
    }

    public boolean esCero(final Integer valor) {
        return obtenerValorDefecto(valor) == 0;
    }

    public String convertirAString(final Integer valor) {
        return String.valueOf(obtenerValorDefecto(valor));
    }

    public Integer convertirDesdeString(final String valor) {
        try {
            return Integer.parseInt(UtilTexto.getInstance().quitarEspaciosEnBlancoInicioFin(valor));
        } catch (final NumberFormatException excepcion) {
            return VALOR_DEFECTO;
        }
    }
}
