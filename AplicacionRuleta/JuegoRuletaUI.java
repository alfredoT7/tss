import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class JuegoRuletaUI extends JFrame {

    private final LogicaRuleta logica = new LogicaRuleta();

    private JSpinner spnCapitalInicial;
    private JSpinner spnMaxRondas;
    private JComboBox<String> cmbVelocidad;

    private JButton btnNuevaSimulacion;
    private JButton btnUnaRonda;
    private JButton btnAutomatico;
    private JButton btnSimularRondas;
    private JButton btnDetener;
    private JButton btnReiniciar;

    private JLabel lblRondaActual;
    private JLabel lblRondasConfiguradas;
    private JLabel lblColorPrimario;
    private JLabel lblColorDecisivo;
    private JLabel lblMensajeGeneral;
    private JLabel lblCapitalFija;
    private JLabel lblApuestaFija;
    private JLabel lblEstadoFija;
    private JLabel lblCapitalProg;
    private JLabel lblApuestaProg;
    private JLabel lblEstadoProg;
    private JLabel lblResumenRondas;
    private JLabel lblResumenFija;
    private JLabel lblResumenProg;
    private JLabel lblResumenGanador;
    private JLabel lblResumenRuinaFija;
    private JLabel lblResumenRuinaProg;

    private JTextArea txtBitacora;
    private CapitalChartPanel panelGrafica;
    private RoulettePanel panelRuleta;
    private Timer timerAutomatico;

    public JuegoRuletaUI() {
        super("Aplicacion 2 - Ruleta | Comparacion de estrategias");
        configurarVentana();
        inicializarComponentes();
        configurarEventos();
        crearTimer();
        iniciarNuevaSimulacion();
    }

    private void configurarVentana() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1350, 860);
        setMinimumSize(new Dimension(980, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));
    }

    private void inicializarComponentes() {
        JPanel principal = new JPanel(new BorderLayout(12, 12));
        principal.setBorder(new EmptyBorder(12, 12, 12, 12));
        principal.setBackground(new Color(245, 247, 250));
        principal.setPreferredSize(new Dimension(1320, 860));
        principal.add(crearEncabezado(), BorderLayout.NORTH);
        principal.add(crearCentro(), BorderLayout.CENTER);
        principal.add(crearResumenActual(), BorderLayout.SOUTH);

        JScrollPane scrollPrincipal = new JScrollPane(principal);
        scrollPrincipal.setBorder(null);
        scrollPrincipal.getVerticalScrollBar().setUnitIncrement(16);
        scrollPrincipal.getHorizontalScrollBar().setUnitIncrement(16);
        setContentPane(scrollPrincipal);
    }

    private JPanel crearEncabezado() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JLabel titulo = new JLabel("Aplicacion 2: Ruleta", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 28));

        JLabel subtitulo = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "Comparacion de dos estrategias apostando siempre al color rojo.<br>"
                        + "Apuesta fija: Bs. " + LogicaRuleta.APUESTA_BASE
                        + " | Progresiva: sube de " + LogicaRuleta.APUESTA_INCREMENTO_PROGRESIVA
                        + " en " + LogicaRuleta.APUESTA_INCREMENTO_PROGRESIVA
                        + " hasta Bs. " + LogicaRuleta.APUESTA_MAXIMA_PROGRESIVA
                        + "</div></html>",
                SwingConstants.CENTER
        );
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 15));

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(subtitulo, BorderLayout.CENTER);
        panel.add(crearPanelControles(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelControles() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(crearBorde("Controles de simulacion"));

        spnCapitalInicial = new JSpinner(new SpinnerNumberModel(LogicaRuleta.CAPITAL_INICIAL_POR_DEFECTO, 1, 100000, 1));
        spnMaxRondas = new JSpinner(new SpinnerNumberModel(LogicaRuleta.MAX_RONDAS_POR_DEFECTO, 1, 100000, 10));
        cmbVelocidad = new JComboBox<>(new String[]{"Lenta", "Media", "Rapida"});

        btnNuevaSimulacion = crearBoton("Nueva simulacion", new Color(41, 128, 185));
        btnUnaRonda = crearBoton("Una ronda", new Color(39, 174, 96));
        btnAutomatico = crearBoton("Automatico", new Color(142, 68, 173));
        btnSimularRondas = crearBoton("Simular rondas", new Color(52, 152, 219));
        btnDetener = crearBoton("Detener", new Color(192, 57, 43));
        btnReiniciar = crearBoton("Reiniciar", new Color(93, 109, 126));

        JPanel filaParametros = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        filaParametros.setOpaque(false);
        filaParametros.add(new JLabel("Capital inicial:"));
        filaParametros.add(spnCapitalInicial);
        filaParametros.add(new JLabel("Numero de rondas a simular:"));
        filaParametros.add(spnMaxRondas);
        filaParametros.add(new JLabel("Velocidad:"));
        filaParametros.add(cmbVelocidad);

        JPanel filaBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        filaBotones.setOpaque(false);
        filaBotones.add(btnNuevaSimulacion);
        filaBotones.add(btnUnaRonda);
        filaBotones.add(btnAutomatico);
        filaBotones.add(btnSimularRondas);
        filaBotones.add(btnDetener);
        filaBotones.add(btnReiniciar);

        panel.add(filaParametros);
        panel.add(Box.createVerticalStrut(4));
        panel.add(filaBotones);
        return panel;
    }

    private JPanel crearCentro() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 12, 12));
        panel.setOpaque(false);
        panel.add(crearPanelEstado());
        panel.add(crearPanelVisualizacion());
        panel.add(crearPanelBitacora());
        return panel;
    }

    private JPanel crearPanelEstado() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(crearBorde("Estado actual de la simulacion"));

        JPanel contenido = new JPanel(new GridLayout(3, 1, 8, 8));
        contenido.setOpaque(false);
        contenido.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel general = new JPanel(new GridLayout(5, 1, 4, 4));
        general.setOpaque(false);
        general.setBorder(crearBordeInterior("Ronda y ruleta"));
        lblRondaActual = crearEtiquetaInfo("Ronda actual: 0");
        lblRondasConfiguradas = crearEtiquetaInfo("Rondas a simular: 0");
        lblColorPrimario = crearEtiquetaInfo("Color primario: -");
        lblColorDecisivo = crearEtiquetaInfo("Color decisivo: -");
        lblMensajeGeneral = crearEtiquetaInfo("Mensaje: Simulacion inicializada");
        general.add(lblRondaActual);
        general.add(lblRondasConfiguradas);
        general.add(lblColorPrimario);
        general.add(lblColorDecisivo);
        general.add(lblMensajeGeneral);

        JPanel fija = new JPanel(new GridLayout(3, 1, 4, 4));
        fija.setOpaque(false);
        fija.setBorder(crearBordeInterior("Estrategia 1 - Apuesta fija"));
        lblCapitalFija = crearEtiquetaInfo("Capital: Bs. 0");
        lblApuestaFija = crearEtiquetaInfo("Apuesta actual: Bs. " + EstrategiaFija.APUESTA_FIJA);
        lblEstadoFija = crearEtiquetaInfo("Estado: Activa");
        fija.add(lblCapitalFija);
        fija.add(lblApuestaFija);
        fija.add(lblEstadoFija);

        JPanel prog = new JPanel(new GridLayout(3, 1, 4, 4));
        prog.setOpaque(false);
        prog.setBorder(crearBordeInterior("Estrategia 2 - Progresiva"));
        lblCapitalProg = crearEtiquetaInfo("Capital: Bs. 0");
        lblApuestaProg = crearEtiquetaInfo("Apuesta actual: Bs. " + EstrategiaProgresiva.APUESTA_INICIAL);
        lblEstadoProg = crearEtiquetaInfo("Estado: Activa");
        prog.add(lblCapitalProg);
        prog.add(lblApuestaProg);
        prog.add(lblEstadoProg);

        contenido.add(general);
        contenido.add(fija);
        contenido.add(prog);
        panel.add(contenido, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelVisualizacion() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(crearBorde("Visualizacion"));

        panelRuleta = new RoulettePanel();
        panelRuleta.setPreferredSize(new Dimension(420, 170));
        panelGrafica = new CapitalChartPanel();
        panelGrafica.setPreferredSize(new Dimension(420, 320));

        JLabel ayuda = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "<b>Lectura visual</b><br>"
                        + "La linea azul muestra la estrategia fija.<br>"
                        + "La linea roja muestra la estrategia progresiva."
                        + "</div></html>",
                SwingConstants.CENTER
        );
        ayuda.setFont(new Font("SansSerif", Font.PLAIN, 13));
        ayuda.setBorder(new EmptyBorder(8, 8, 8, 8));

        panel.add(panelRuleta, BorderLayout.NORTH);
        panel.add(panelGrafica, BorderLayout.CENTER);
        panel.add(ayuda, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelBitacora() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(crearBorde("Bitacora de la simulacion"));
        txtBitacora = new JTextArea();
        txtBitacora.setEditable(false);
        txtBitacora.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtBitacora.setLineWrap(true);
        txtBitacora.setWrapStyleWord(true);
        panel.add(new JScrollPane(txtBitacora), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearResumenActual() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 12, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(crearBorde("Resumen de la simulacion actual"));

        lblResumenRondas = crearEtiquetaResumen("Rondas simuladas: 0");
        lblResumenFija = crearEtiquetaResumen("Capital final fija: Bs. 0");
        lblResumenProg = crearEtiquetaResumen("Capital final progresiva: Bs. 0");
        lblResumenGanador = crearEtiquetaResumen("Ganador: -");
        lblResumenRuinaFija = crearEtiquetaResumen("Ruina fija: No");
        lblResumenRuinaProg = crearEtiquetaResumen("Ruina progresiva: No");

        panel.add(lblResumenRondas);
        panel.add(lblResumenFija);
        panel.add(lblResumenProg);
        panel.add(lblResumenGanador);
        panel.add(lblResumenRuinaFija);
        panel.add(lblResumenRuinaProg);
        return panel;
    }

    private void configurarEventos() {
        btnNuevaSimulacion.addActionListener(e -> iniciarNuevaSimulacion());
        btnUnaRonda.addActionListener(e -> ejecutarUnaRondaVisual());
        btnAutomatico.addActionListener(e -> iniciarAutomatico());
        btnSimularRondas.addActionListener(e -> simularTodasLasRondas());
        btnDetener.addActionListener(e -> detenerAutomatico());
        btnReiniciar.addActionListener(e -> iniciarNuevaSimulacion());
    }

    private void crearTimer() {
        timerAutomatico = new Timer(350, e -> ejecutarUnaRondaVisual());
    }

    private void iniciarNuevaSimulacion() {
        timerAutomatico.stop();
        int capital = (Integer) spnCapitalInicial.getValue();
        int maxRondas = (Integer) spnMaxRondas.getValue();
        logica.nuevaSimulacion(capital, maxRondas);

        txtBitacora.setText("");
        escribirBitacora("Nueva simulacion iniciada.");
        escribirBitacora("Capital inicial para ambas estrategias: Bs. " + capital);
        escribirBitacora("Numero de rondas a simular: " + maxRondas);
        escribirBitacora("Apuesta fija: Bs. " + LogicaRuleta.APUESTA_BASE);
        escribirBitacora("Apuesta progresiva inicia en Bs. " + LogicaRuleta.APUESTA_BASE
                + " y sube de " + LogicaRuleta.APUESTA_INCREMENTO_PROGRESIVA
                + " en " + LogicaRuleta.APUESTA_INCREMENTO_PROGRESIVA
                + " hasta Bs. " + LogicaRuleta.APUESTA_MAXIMA_PROGRESIVA);

        actualizarPantallaEstado("Simulacion lista para comenzar.");
        refrescarPaneles();
    }

    private void ejecutarUnaRondaVisual() {
        if (!logica.simulacionActiva) {
            iniciarNuevaSimulacion();
        }

        LogicaRuleta.EventoRonda evento = logica.ejecutarRonda();
        for (String mensaje : evento.mensajes) {
            escribirBitacora(mensaje);
        }

        actualizarPantallaEstado("Ronda procesada.");
        if (evento.simulacionTerminada) {
            actualizarPantallaEstado("Simulacion finalizada.");
            timerAutomatico.stop();
        }
        refrescarPaneles();
    }

    private void iniciarAutomatico() {
        if (!logica.simulacionActiva) {
            iniciarNuevaSimulacion();
        }
        timerAutomatico.setDelay(obtenerDelaySeleccionado());
        timerAutomatico.start();
        escribirBitacora("Modo automatico activado.");
    }

    private void simularTodasLasRondas() {
        timerAutomatico.stop();
        if (!logica.simulacionActiva) {
            iniciarNuevaSimulacion();
        }

        escribirBitacora("Simulando rondas restantes...");
        while (logica.simulacionActiva) {
            LogicaRuleta.EventoRonda evento = logica.ejecutarRonda();
            for (String mensaje : evento.mensajes) {
                escribirBitacora(mensaje);
            }
        }

        actualizarPantallaEstado("Simulacion finalizada.");
        refrescarPaneles();
    }

    private void detenerAutomatico() {
        timerAutomatico.stop();
        escribirBitacora("Modo automatico detenido.");
    }

    private int obtenerDelaySeleccionado() {
        String velocidad = (String) cmbVelocidad.getSelectedItem();
        if ("Lenta".equals(velocidad)) {
            return 900;
        }
        if ("Media".equals(velocidad)) {
            return 350;
        }
        return 120;
    }

    private void actualizarPantallaEstado(String mensaje) {
        lblRondaActual.setText("Ronda actual: " + logica.rondaActual);
        lblRondasConfiguradas.setText("Rondas a simular: " + logica.maxRondas);
        lblColorPrimario.setText("Color primario: " + obtenerColorPrimarioTexto());
        lblColorDecisivo.setText("Color decisivo: " + obtenerColorDecisivoTexto());
        lblMensajeGeneral.setText("Mensaje: " + mensaje);

        lblCapitalFija.setText("Capital: Bs. " + logica.jugadorFija.capital);
        lblApuestaFija.setText("Apuesta actual: Bs. " + logica.jugadorFija.apuestaActual);
        lblEstadoFija.setText("Estado: " + LogicaRuleta.estadoJugadorTexto(logica.jugadorFija));

        lblCapitalProg.setText("Capital: Bs. " + logica.jugadorProg.capital);
        lblApuestaProg.setText("Apuesta actual: Bs. " + logica.jugadorProg.apuestaActual);
        lblEstadoProg.setText("Estado: " + LogicaRuleta.estadoJugadorTexto(logica.jugadorProg));
        actualizarResumenActual();
    }

    private String obtenerColorPrimarioTexto() {
        if (logica.ultimoResultado == null) {
            return "-";
        }
        return LogicaRuleta.colorTexto(logica.ultimoResultado.colorPrimario);
    }

    private String obtenerColorDecisivoTexto() {
        if (logica.ultimoResultado == null) {
            return "-";
        }
        return LogicaRuleta.colorTexto(logica.ultimoResultado.colorDecisivo);
    }

    private void actualizarResumenActual() {
        lblResumenRondas.setText("Rondas simuladas: " + logica.rondaActual + " de " + logica.maxRondas);
        lblResumenFija.setText("Capital final fija: Bs. " + logica.jugadorFija.capital);
        lblResumenProg.setText("Capital final progresiva: Bs. " + logica.jugadorProg.capital);
        lblResumenGanador.setText("Ganador: " + ganadorTexto());
        lblResumenRuinaFija.setText("Ruina fija: " + siNo(logica.jugadorFija.arruinada));
        lblResumenRuinaProg.setText("Ruina progresiva: " + siNo(logica.jugadorProg.arruinada));
    }

    private String ganadorTexto() {
        if (logica.jugadorFija.capital > logica.jugadorProg.capital) {
            return "Fija";
        }
        if (logica.jugadorProg.capital > logica.jugadorFija.capital) {
            return "Progresiva";
        }
        return "Empate";
    }

    private String siNo(boolean valor) {
        if (valor) {
            return "Si";
        }
        return "No";
    }

    private void escribirBitacora(String texto) {
        txtBitacora.append(texto + "\n");
        txtBitacora.setCaretPosition(txtBitacora.getDocument().getLength());
    }

    private void refrescarPaneles() {
        panelGrafica.repaint();
        panelRuleta.repaint();
    }

    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFocusPainted(false);
        boton.setForeground(Color.WHITE);
        boton.setBackground(color);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBorderPainted(false);
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setBorder(new EmptyBorder(10, 16, 10, 16));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    private JLabel crearEtiquetaInfo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 16));
        return lbl;
    }

    private JLabel crearEtiquetaResumen(String texto) {
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        lbl.setOpaque(true);
        lbl.setBackground(new Color(248, 249, 250));
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 223)),
                new EmptyBorder(14, 8, 14, 8)
        ));
        return lbl;
    }

    private TitledBorder crearBorde(String titulo) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 205)),
                titulo,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13)
        );
    }

    private TitledBorder crearBordeInterior(String titulo) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(205, 210, 218)),
                titulo,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12)
        );
    }

    private class RoulettePanel extends JPanel {
        RoulettePanel() {
            setPreferredSize(new Dimension(420, 180));
            setBackground(Color.WHITE);
            setBorder(crearBordeInterior("Ultimo resultado de la ruleta"));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            g2.setColor(new Color(245, 247, 250));
            g2.fillRoundRect(15, 15, w - 30, h - 30, 18, 18);

            dibujarCirculo(g2, 100, h / 2, 70, colorPrimario(), "Primario");
            dibujarCirculo(g2, w - 170, h / 2, 70, colorDecisivo(), "Decisivo");

            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString("Apuesta para ambos jugadores: ROJO", w / 2 - 125, 35);

            if (logica.ultimoResultado != null) {
                g2.drawString("Efecto final: " + LogicaRuleta.resultadoTexto(logica.ultimoResultado.resultado),
                        w / 2 - 70, h - 20);
            }

            g2.dispose();
        }

        private void dibujarCirculo(Graphics2D g2, int cx, int cy, int d, Color color, String texto) {
            g2.setColor(color);
            g2.fillOval(cx, cy - d / 2, d, d);
            g2.setColor(Color.DARK_GRAY);
            g2.drawOval(cx, cy - d / 2, d, d);
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2.drawString(texto, cx + 10, cy + d / 2 + 18);
        }

        private Color colorPrimario() {
            if (logica.ultimoResultado == null) {
                return Color.LIGHT_GRAY;
            }
            return colorSwing(logica.ultimoResultado.colorPrimario);
        }

        private Color colorDecisivo() {
            if (logica.ultimoResultado == null) {
                return Color.LIGHT_GRAY;
            }
            return colorSwing(logica.ultimoResultado.colorDecisivo);
        }

        private Color colorSwing(LogicaRuleta.ColorRuleta c) {
            if (c == LogicaRuleta.ColorRuleta.ROJO) {
                return new Color(192, 57, 43);
            }
            if (c == LogicaRuleta.ColorRuleta.NEGRO) {
                return new Color(52, 73, 94);
            }
            return new Color(39, 174, 96);
        }
    }

    private class CapitalChartPanel extends JPanel {
        CapitalChartPanel() {
            setBackground(Color.WHITE);
            setBorder(crearBordeInterior("Evolucion del capital"));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int m = 45;

            g2.setColor(new Color(245, 247, 250));
            g2.fillRoundRect(15, 15, w - 30, h - 30, 18, 18);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(25, 25, w - 50, h - 50, 18, 18);

            int x0 = m;
            int y0 = h - m;
            int xMax = w - m;
            int yMax = m;

            g2.setColor(Color.DARK_GRAY);
            g2.drawLine(x0, y0, xMax, y0);
            g2.drawLine(x0, y0, x0, yMax);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.drawString("Rondas", xMax - 40, y0 + 25);
            g2.drawString("Capital", 10, yMax - 8);

            int maxCapitalGrafica = Math.max(logica.capitalInicial + 50, 50);
            dibujarGuias(g2, x0, y0, xMax, yMax, maxCapitalGrafica);
            dibujarSerie(g2, logica.historialFija, x0, y0, xMax, yMax, maxCapitalGrafica, new Color(41, 128, 185));
            dibujarSerie(g2, logica.historialProg, x0, y0, xMax, yMax, maxCapitalGrafica, new Color(231, 76, 60));
            dibujarLeyenda(g2, x0, yMax);

            g2.dispose();
        }

        private void dibujarGuias(Graphics2D g2, int x0, int y0, int xMax, int yMax, int maxCapitalGrafica) {
            for (int i = 0; i <= 5; i++) {
                int ref = (int) Math.round((double) maxCapitalGrafica * i / 5.0);
                int y = y0 - (int) ((double) ref / maxCapitalGrafica * (y0 - yMax));
                g2.setColor(new Color(230, 233, 238));
                g2.drawLine(x0, y, xMax, y);
                g2.setColor(Color.GRAY);
                g2.drawString(String.valueOf(ref), x0 - 35, y + 4);
            }
        }

        private void dibujarLeyenda(Graphics2D g2, int x0, int yMax) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(new Color(41, 128, 185));
            g2.fillRect(x0 + 20, yMax + 10, 18, 8);
            g2.drawString("Fija", x0 + 45, yMax + 18);
            g2.setColor(new Color(231, 76, 60));
            g2.fillRect(x0 + 110, yMax + 10, 18, 8);
            g2.drawString("Progresiva", x0 + 135, yMax + 18);
        }

        private void dibujarSerie(Graphics2D g2, List<Integer> serie, int x0, int y0, int xMax, int yMax,
                                  int maxCapital, Color color) {
            if (serie.size() < 2) {
                return;
            }

            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.4f));
            int ancho = xMax - x0;
            int alto = y0 - yMax;

            for (int i = 0; i < serie.size() - 1; i++) {
                int x1 = x0 + (int) ((double) i / Math.max(1, serie.size() - 1) * ancho);
                int x2 = x0 + (int) ((double) (i + 1) / Math.max(1, serie.size() - 1) * ancho);
                int y1 = y0 - (int) ((double) serie.get(i) / Math.max(1, maxCapital) * alto);
                int y2 = y0 - (int) ((double) serie.get(i + 1) / Math.max(1, maxCapital) * alto);
                g2.drawLine(x1, y1, x2, y2);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JuegoRuletaUI().setVisible(true));
    }
}
