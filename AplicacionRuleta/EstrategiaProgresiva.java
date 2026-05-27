public class EstrategiaProgresiva {

    public static final String NOMBRE = "Progresiva";
    public static final int APUESTA_INICIAL = LogicaRuleta.APUESTA_BASE;
    public static final int APUESTA_INCREMENTO = LogicaRuleta.APUESTA_INCREMENTO_PROGRESIVA;
    public static final int APUESTA_MAXIMA = LogicaRuleta.APUESTA_MAXIMA_PROGRESIVA;

    public int obtenerApuesta(int apuestaActual) {
        return apuestaActual;
    }

    public int calcularSiguienteApuesta(LogicaRuleta.ResultadoApuesta resultado, int apuestaActual) {
        if (resultado == LogicaRuleta.ResultadoApuesta.GANA) {
            return APUESTA_INICIAL;
        }

        if (resultado == LogicaRuleta.ResultadoApuesta.EMPATE) {
            return apuestaActual;
        }

        if (apuestaActual >= APUESTA_MAXIMA) {
            return APUESTA_INICIAL;
        }

        int siguiente = apuestaActual + APUESTA_INCREMENTO;
        if (siguiente > APUESTA_MAXIMA) {
            return APUESTA_MAXIMA;
        }
        return siguiente;
    }
}
