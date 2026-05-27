public class EstrategiaFija {

    public static final String NOMBRE = "Fija";
    public static final int APUESTA_FIJA = LogicaRuleta.APUESTA_BASE;

    public int obtenerApuesta(int apuestaActual) {
        return APUESTA_FIJA;
    }

    public int calcularSiguienteApuesta(LogicaRuleta.ResultadoApuesta resultado, int apuestaActual) {
        return APUESTA_FIJA;
    }
}
