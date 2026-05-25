import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Toda la lógica del juego 7-11.
 * Cambiar los parámetros de la sección "CONFIGURACIÓN" para ajustar el juego.
 */
public class SimuladorJuego {

    // =========================================================================
    // CONFIGURACIÓN — modificar estos valores para cambiar el comportamiento
    // =========================================================================

    /** Capital con el que el jugador empieza cada corrida */
    public static final int CAPITAL_INICIAL = 20;

    /** Capital que debe alcanzar el jugador para ganar (meta) */
    public static final int META_CAPITAL = 50;

    /** Cuánto se gana o pierde por partida */
    public static final int APUESTA = 1;

    // =========================================================================
    // Estado de la corrida visual (la UI lee estos campos para mostrar pantalla)
    // =========================================================================

    public int capitalActual;       // capital del jugador en este momento
    public int partidaActual;       // número de partida dentro de la corrida
    public int lanzamientoActual;   // número de lanzamiento dentro de la partida
    public boolean enFasePunto;     // true = ya se estableció un punto, buscando repetirlo o sacar 7
    public int puntoActual;         // valor del punto establecido (-1 si no hay punto)
    public boolean activa;          // true = la corrida sigue en curso
    public boolean contabilizada;   // true = el resultado ya fue registrado en estadísticas
    public int dado1;               // valor del primer dado en el último lanzamiento
    public int dado2;               // valor del segundo dado en el último lanzamiento
    public int sumaDados;           // suma de los dos dados en el último lanzamiento
    public final List<Integer> historialCapital = new ArrayList<>(); // capital tras cada partida

    private int totalLanzamientosVisual; // contador interno de lanzamientos de esta corrida
    private final Random random = new Random();

    // =========================================================================
    // Estadísticas globales acumuladas (todas las corridas juntas)
    // =========================================================================

    public int totalCorridas;              // cuántas corridas se han completado
    public int totalQuiebras;              // cuántas terminaron en quiebra (capital = 0)
    public int totalExitos;                // cuántas alcanzaron la meta
    public long totalPartidasGlobal;       // suma de partidas jugadas en todas las corridas
    public long totalLanzamientosGlobal;   // suma de lanzamientos en todas las corridas

    // =========================================================================
    // Control de la corrida visual paso a paso
    // =========================================================================

    /**
     * Reinicia el estado para comenzar una corrida nueva.
     * Llamar antes de ejecutar lanzamientos visuales.
     */
    public void nuevaCorrida() {
        capitalActual = CAPITAL_INICIAL;
        partidaActual = 1;
        lanzamientoActual = 0;
        totalLanzamientosVisual = 0;
        enFasePunto = false;
        puntoActual = -1;
        activa = true;
        contabilizada = false;
        dado1 = 1;
        dado2 = 1;
        sumaDados = 0;
        historialCapital.clear();
        historialCapital.add(capitalActual);
    }

    /**
     * Indica si la corrida ya terminó (quiebra o meta alcanzada).
     */
    public boolean corridaTerminada() {
        return capitalActual <= 0 || capitalActual >= META_CAPITAL;
    }

    /**
     * Ejecuta un solo lanzamiento de dados y actualiza el estado.
     * Devuelve un EventoLanzamiento con lo que ocurrió para que la UI lo muestre.
     */
    public EventoLanzamiento ejecutarLanzamiento() {
        EventoLanzamiento evento = new EventoLanzamiento();
        lanzamientoActual++;
        totalLanzamientosVisual++;

        dado1 = lanzarDado();
        dado2 = lanzarDado();
        sumaDados = dado1 + dado2;

        evento.partida = partidaActual;
        evento.lanzamiento = lanzamientoActual;
        evento.dado1 = dado1;
        evento.dado2 = dado2;
        evento.sumaDados = sumaDados;

        if (!enFasePunto) {
            resolverPrimerLanzamiento(evento);
        } else {
            resolverFasePunto(evento);
        }

        evento.terminada = corridaTerminada();
        return evento;
    }

    /**
     * Resuelve el primer lanzamiento de una partida:
     * - 7 u 11 → gana la partida
     * - 2, 3 o 12 → pierde la partida
     * - cualquier otro → establece ese valor como "punto" y la partida continúa
     */
    private void resolverPrimerLanzamiento(EventoLanzamiento evento) {
        if (sumaDados == 7 || sumaDados == 11) {
            evento.mensajes.add("Resultado de la partida: GANADA en el primer lanzamiento.");
            cerrarPartida(true, evento);
        } else if (sumaDados == 2 || sumaDados == 3 || sumaDados == 12) {
            evento.mensajes.add("Resultado de la partida: PERDIDA en el primer lanzamiento.");
            cerrarPartida(false, evento);
        } else {
            enFasePunto = true;
            puntoActual = sumaDados;
            evento.mensajes.add("Se establece punto = " + puntoActual + ". La partida continúa.");
        }
    }

    /**
     * Resuelve un lanzamiento en fase de punto:
     * - repite el punto → gana la partida
     * - sale 7 → pierde la partida
     * - cualquier otro → la partida sigue
     */
    private void resolverFasePunto(EventoLanzamiento evento) {
        if (sumaDados == puntoActual) {
            evento.mensajes.add("Reapareció el punto " + puntoActual + ". Partida GANADA.");
            cerrarPartida(true, evento);
        } else if (sumaDados == 7) {
            evento.mensajes.add("Apareció 7 antes del punto. Partida PERDIDA.");
            cerrarPartida(false, evento);
        } else {
            evento.mensajes.add("No salió ni el punto ni 7. La partida continúa.");
        }
    }

    /**
     * Cierra la partida actual: ajusta capital, registra en historial,
     * resetea el estado para la siguiente partida.
     */
    private void cerrarPartida(boolean gano, EventoLanzamiento evento) {
        capitalActual += gano ? APUESTA : -APUESTA;
        historialCapital.add(capitalActual);
        evento.mensajes.add("Capital actualizado: Bs. " + capitalActual + (gano ? "  (+1)" : "  (-1)"));
        enFasePunto = false;
        puntoActual = -1;
        partidaActual++;
        lanzamientoActual = 0;
    }

    /**
     * Marca la corrida como terminada y devuelve su resultado.
     * Llamar cuando corridaTerminada() sea true, antes de registrarCorrida().
     */
    public ResultadoCorrida finalizarCorrida() {
        activa = false;
        contabilizada = true;
        return new ResultadoCorrida(
                capitalActual <= 0,
                partidaActual - 1,
                totalLanzamientosVisual,
                capitalActual
        );
    }

    // =========================================================================
    // Simulación en lote (miles de corridas rápidas para estimar probabilidad)
    // =========================================================================

    /**
     * Simula N corridas completas sin visualización.
     * Usa la misma lógica de dados internamente pero sin actualizar el estado visual.
     * Devuelve estadísticas del lote para acumular en los totales globales.
     */
    public ResultadoLote simularLote(int corridas) {
        int quiebras = 0, exitos = 0;
        long totalPartidas = 0, totalLanzamientos = 0, capitalFinalAcumulado = 0;

        for (int i = 0; i < corridas; i++) {
            int capital = CAPITAL_INICIAL;
            int partidas = 0;
            int lanzamientos = 0;

            while (capital > 0 && capital < META_CAPITAL) {
                partidas++;
                // Primer lanzamiento de la partida
                int suma = lanzarDado() + lanzarDado();
                lanzamientos++;

                if (suma == 7 || suma == 11) {
                    capital += APUESTA;
                } else if (suma == 2 || suma == 3 || suma == 12) {
                    capital -= APUESTA;
                } else {
                    // Fase punto: seguir hasta repetir la suma o sacar 7
                    int punto = suma;
                    boolean resuelta = false;
                    while (!resuelta) {
                        int nueva = lanzarDado() + lanzarDado();
                        lanzamientos++;
                        if (nueva == punto) {
                            capital += APUESTA;
                            resuelta = true;
                        } else if (nueva == 7) {
                            capital -= APUESTA;
                            resuelta = true;
                        }
                    }
                }
            }

            totalPartidas += partidas;
            totalLanzamientos += lanzamientos;
            capitalFinalAcumulado += capital;
            if (capital <= 0) quiebras++;
            else exitos++;
        }

        return new ResultadoLote(corridas, quiebras, exitos,
                totalPartidas, totalLanzamientos, capitalFinalAcumulado);
    }

    // =========================================================================
    // Estadísticas — acumulan resultados de corridas visuales y lotes
    // =========================================================================

    /**
     * Registra el resultado de una corrida visual en los totales globales.
     */
    public void registrarCorrida(ResultadoCorrida resultado) {
        totalCorridas++;
        totalPartidasGlobal += resultado.partidasJugadas;
        totalLanzamientosGlobal += resultado.lanzamientosTotales;
        if (resultado.quiebra) totalQuiebras++;
        else totalExitos++;
    }

    /**
     * Suma los resultados de un lote a los totales globales.
     */
    public void acumularLote(ResultadoLote lote) {
        totalCorridas += lote.corridas;
        totalQuiebras += lote.quiebras;
        totalExitos += lote.exitos;
        totalPartidasGlobal += lote.partidas;
        totalLanzamientosGlobal += lote.lanzamientos;
    }

    /**
     * Pone a cero todos los contadores globales.
     */
    public void reiniciarEstadisticas() {
        totalCorridas = 0;
        totalQuiebras = 0;
        totalExitos = 0;
        totalPartidasGlobal = 0;
        totalLanzamientosGlobal = 0;
    }

    /** Probabilidad de quiebra = quiebras / corridas totales */
    public double probabilidadQuiebra() {
        return totalCorridas == 0 ? 0.0 : (double) totalQuiebras / totalCorridas;
    }

    /** Promedio de partidas por corrida */
    public double promedioPartidas() {
        return totalCorridas == 0 ? 0.0 : (double) totalPartidasGlobal / totalCorridas;
    }

    /** Promedio de lanzamientos por corrida */
    public double promedioLanzamientos() {
        return totalCorridas == 0 ? 0.0 : (double) totalLanzamientosGlobal / totalCorridas;
    }

    // =========================================================================
    // Dados
    // =========================================================================

    /** Lanza un dado de 6 caras. Devuelve valor entre 1 y 6. */
    private int lanzarDado() {
        return random.nextInt(6) + 1;
    }

    // =========================================================================
    // Clases de datos (resultados que devuelven los métodos)
    // =========================================================================

    /** Resultado de un solo lanzamiento de dados (para la UI paso a paso) */
    public static class EventoLanzamiento {
        public int partida;
        public int lanzamiento;
        public int dado1;
        public int dado2;
        public int sumaDados;
        public boolean terminada;                             // true = la corrida acabó con este lanzamiento
        public final List<String> mensajes = new ArrayList<>(); // mensajes a mostrar en bitácora
    }

    /** Resultado de una corrida completa (visual o de lote) */
    public static class ResultadoCorrida {
        public final boolean quiebra;           // true = terminó sin capital
        public final int partidasJugadas;       // cuántas partidas se jugaron
        public final int lanzamientosTotales;   // cuántos dados se lanzaron en total
        public final int capitalFinal;          // capital con el que terminó

        public ResultadoCorrida(boolean quiebra, int partidasJugadas,
                                int lanzamientosTotales, int capitalFinal) {
            this.quiebra = quiebra;
            this.partidasJugadas = partidasJugadas;
            this.lanzamientosTotales = lanzamientosTotales;
            this.capitalFinal = capitalFinal;
        }
    }

    /** Resultado de simular un lote de N corridas */
    public static class ResultadoLote {
        public final int corridas;                  // cuántas corridas se simularon
        public final int quiebras;                  // cuántas terminaron en quiebra
        public final int exitos;                    // cuántas alcanzaron la meta
        public final long partidas;                 // suma de partidas de todas las corridas
        public final long lanzamientos;             // suma de lanzamientos de todas las corridas
        public final long capitalFinalAcumulado;    // suma de capital final de todas las corridas

        public ResultadoLote(int corridas, int quiebras, int exitos,
                             long partidas, long lanzamientos, long capitalFinalAcumulado) {
            this.corridas = corridas;
            this.quiebras = quiebras;
            this.exitos = exitos;
            this.partidas = partidas;
            this.lanzamientos = lanzamientos;
            this.capitalFinalAcumulado = capitalFinalAcumulado;
        }
    }
}
