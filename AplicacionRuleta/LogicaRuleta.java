import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LogicaRuleta {

    public static final int CANTIDAD_ROJOS = 10;
    public static final int CANTIDAD_NEGROS = 10;
    public static final int CANTIDAD_VERDES = 2;
    public static final int TOTAL_CASILLAS = CANTIDAD_ROJOS + CANTIDAD_NEGROS + CANTIDAD_VERDES;
    public static final int CAPITAL_INICIAL_POR_DEFECTO = 200;
    public static final int MAX_RONDAS_POR_DEFECTO = 300;
    public static final int APUESTA_BASE = 1;
    public static final int APUESTA_INCREMENTO_PROGRESIVA = APUESTA_BASE;
    public static final int APUESTA_MAXIMA_PROGRESIVA = 500;

    private final Random random = new Random();
    private final EstrategiaFija estrategiaFija = new EstrategiaFija();
    private final EstrategiaProgresiva estrategiaProgresiva = new EstrategiaProgresiva();

    public int capitalInicial;
    public int maxRondas;
    public int rondaActual;
    public boolean simulacionActiva;
    public EstadoJugador jugadorFija;
    public EstadoJugador jugadorProg;
    public ResolucionRonda ultimoResultado;
    public final List<Integer> historialFija = new ArrayList<>();
    public final List<Integer> historialProg = new ArrayList<>();

    public void nuevaSimulacion(int capitalInicial, int maxRondas) {
        this.capitalInicial = capitalInicial;
        this.maxRondas = maxRondas;
        this.rondaActual = 0;
        this.simulacionActiva = true;
        this.ultimoResultado = null;
        this.jugadorFija = crearJugadorFija(capitalInicial);
        this.jugadorProg = crearJugadorProgresiva(capitalInicial);

        historialFija.clear();
        historialProg.clear();
        historialFija.add(jugadorFija.capital);
        historialProg.add(jugadorProg.capital);
    }

    public boolean simulacionDebeTerminar() {
        if (rondaActual >= maxRondas) {
            return true;
        }
        if (!jugadorFija.activa && !jugadorProg.activa) {
            return true;
        }
        return false;
    }

    public EventoRonda ejecutarRonda() {
        EventoRonda evento = new EventoRonda();

        if (!simulacionActiva) {
            evento.mensajes.add("La simulacion no esta activa.");
            return evento;
        }

        if (simulacionDebeTerminar()) {
            finalizarSimulacion(evento);
            return evento;
        }

        rondaActual++;
        ultimoResultado = resolverRondaContraRojo();

        evento.ronda = rondaActual;
        evento.resolucion = ultimoResultado;
        evento.mensajes.add("Ronda " + rondaActual
                + " -> Color primario: " + colorTexto(ultimoResultado.colorPrimario)
                + " | Color decisivo: " + colorTexto(ultimoResultado.colorDecisivo)
                + " | Efecto final: " + resultadoTexto(ultimoResultado.resultado));

        aplicarResultado(jugadorFija, ultimoResultado, evento);
        aplicarResultado(jugadorProg, ultimoResultado, evento);

        historialFija.add(jugadorFija.capital);
        historialProg.add(jugadorProg.capital);

        if (simulacionDebeTerminar()) {
            finalizarSimulacion(evento);
        }

        return evento;
    }

    public void ejecutarTodasLasRondas() {
        while (simulacionActiva) {
            ejecutarRonda();
        }
    }

    private void finalizarSimulacion(EventoRonda evento) {
        simulacionActiva = false;
        evento.simulacionTerminada = true;
        evento.mensajes.add(resultadoSimulacionTexto());
    }

    public EstadoJugador crearJugadorFija(int capitalInicial) {
        return new EstadoJugador(EstrategiaFija.NOMBRE, capitalInicial, EstrategiaFija.APUESTA_FIJA, true);
    }

    public EstadoJugador crearJugadorProgresiva(int capitalInicial) {
        return new EstadoJugador(EstrategiaProgresiva.NOMBRE, capitalInicial,
                EstrategiaProgresiva.APUESTA_INICIAL, false);
    }

    public ResolucionRonda resolverRondaContraRojo() {
        ColorRuleta primario = girarColor();

        if (primario == ColorRuleta.ROJO) {
            return new ResolucionRonda(primario, primario, ResultadoApuesta.GANA);
        }

        if (primario == ColorRuleta.NEGRO) {
            return new ResolucionRonda(primario, primario, ResultadoApuesta.PIERDE);
        }

        ColorRuleta decisivo = girarColor();
        while (decisivo == ColorRuleta.VERDE) {
            decisivo = girarColor();
        }

        if (decisivo == ColorRuleta.ROJO) {
            return new ResolucionRonda(primario, decisivo, ResultadoApuesta.EMPATE);
        }
        return new ResolucionRonda(primario, decisivo, ResultadoApuesta.PIERDE);
    }

    private ColorRuleta girarColor() {
        int n = random.nextInt(TOTAL_CASILLAS);
        if (n < CANTIDAD_ROJOS) {
            return ColorRuleta.ROJO;
        }
        if (n < CANTIDAD_ROJOS + CANTIDAD_NEGROS) {
            return ColorRuleta.NEGRO;
        }
        return ColorRuleta.VERDE;
    }

    private void aplicarResultado(EstadoJugador jugador, ResolucionRonda rr, EventoRonda eventoRonda) {
        if (!jugador.activa) {
            return;
        }

        int apuesta = obtenerApuesta(jugador);
        if (jugador.capital < apuesta) {
            apuesta = ajustarApuestaPorCapital(jugador, apuesta);
        }

        if (apuesta <= 0) {
            arruinarSinApuesta(jugador, eventoRonda);
            return;
        }

        if (rr.resultado == ResultadoApuesta.GANA) {
            jugador.capital += apuesta;
            jugador.ultimaAccion = "Gana Bs. " + apuesta;
        } else if (rr.resultado == ResultadoApuesta.PIERDE) {
            jugador.capital -= apuesta;
            jugador.ultimaAccion = "Pierde Bs. " + apuesta;
        } else {
            jugador.ultimaAccion = "No gana ni pierde";
        }

        if (!jugador.estrategiaFija) {
            jugador.apuestaActual = estrategiaProgresiva.calcularSiguienteApuesta(rr.resultado, apuesta);
        } else {
            jugador.apuestaActual = estrategiaFija.calcularSiguienteApuesta(rr.resultado, apuesta);
        }

        if (jugador.capital <= 0) {
            jugador.capital = 0;
            jugador.activa = false;
            jugador.arruinada = true;
        }

        agregarMensajeJugador(jugador, eventoRonda);
    }

    private int obtenerApuesta(EstadoJugador jugador) {
        if (jugador.estrategiaFija) {
            return estrategiaFija.obtenerApuesta(jugador.apuestaActual);
        }
        return estrategiaProgresiva.obtenerApuesta(jugador.apuestaActual);
    }

    private int ajustarApuestaPorCapital(EstadoJugador jugador, int apuesta) {
        if (jugador.estrategiaFija) {
            return apuesta;
        }
        return jugador.capital;
    }

    private void arruinarSinApuesta(EstadoJugador jugador, EventoRonda eventoRonda) {
        jugador.activa = false;
        jugador.arruinada = true;
        jugador.capital = 0;
        jugador.ultimaAccion = "Sin capital para apostar";
        agregarMensajeJugador(jugador, eventoRonda);
    }

    private void agregarMensajeJugador(EstadoJugador jugador, EventoRonda eventoRonda) {
        if (eventoRonda == null) {
            return;
        }

        eventoRonda.mensajes.add("Estrategia " + jugador.nombre
                + " -> " + jugador.ultimaAccion
                + " | Capital: Bs. " + jugador.capital
                + " | Proxima apuesta: Bs. " + jugador.apuestaActual);
    }

    public String resultadoSimulacionTexto() {
        if (jugadorFija.capital > jugadorProg.capital) {
            return "Resultado final: Gana la estrategia fija.";
        }
        if (jugadorProg.capital > jugadorFija.capital) {
            return "Resultado final: Gana la estrategia progresiva.";
        }
        return "Resultado final: Empate.";
    }

    public static String estadoJugadorTexto(EstadoJugador j) {
        if (j.arruinada) {
            return "Sin capital";
        }
        if (!j.activa) {
            return "Inactivo";
        }
        return "Activa";
    }

    public static String colorTexto(ColorRuleta c) {
        if (c == null) {
            return "-";
        }
        if (c == ColorRuleta.ROJO) {
            return "Rojo";
        }
        if (c == ColorRuleta.NEGRO) {
            return "Negro";
        }
        return "Verde";
    }

    public static String resultadoTexto(ResultadoApuesta resultado) {
        if (resultado == ResultadoApuesta.GANA) {
            return "Gana";
        }
        if (resultado == ResultadoApuesta.PIERDE) {
            return "Pierde";
        }
        return "Empate";
    }

    public static class EventoRonda {
        public int ronda;
        public ResolucionRonda resolucion;
        public boolean simulacionTerminada;
        public final List<String> mensajes = new ArrayList<>();
    }

    public static class EstadoJugador {
        public String nombre;
        public int capital;
        public int apuestaActual;
        public boolean estrategiaFija;
        public boolean activa;
        public boolean arruinada;
        public String ultimaAccion;

        public EstadoJugador(String nombre, int capital, int apuestaActual, boolean estrategiaFija) {
            this.nombre = nombre;
            this.capital = capital;
            this.apuestaActual = apuestaActual;
            this.estrategiaFija = estrategiaFija;
            this.activa = true;
            this.arruinada = false;
            this.ultimaAccion = "-";
        }
    }

    public static class ResolucionRonda {
        public ColorRuleta colorPrimario;
        public ColorRuleta colorDecisivo;
        public ResultadoApuesta resultado;

        public ResolucionRonda(ColorRuleta colorPrimario, ColorRuleta colorDecisivo, ResultadoApuesta resultado) {
            this.colorPrimario = colorPrimario;
            this.colorDecisivo = colorDecisivo;
            this.resultado = resultado;
        }
    }

    public enum ColorRuleta {
        ROJO, NEGRO, VERDE
    }

    public enum ResultadoApuesta {
        GANA, PIERDE, EMPATE
    }
}
