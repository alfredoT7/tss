import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class JuegoDadosUI extends JFrame {

    private final DecimalFormat df = new DecimalFormat("0.0000");

    // Toda la lógica del juego vive en SimuladorJuego
    private final SimuladorJuego sim = new SimuladorJuego();

    // Componentes UI
    private JLabel lblCapitalActual;
    private JLabel lblPartidaActual;
    private JLabel lblLanzamientoActual;
    private JLabel lblEstadoPartida;
    private JLabel lblPuntoActual;
    private JLabel lblDados;
    private JLabel lblSuma;
    private JLabel lblMensajePrincipal;

    private JLabel lblTotalCorridas;
    private JLabel lblTotalQuiebras;
    private JLabel lblTotalExitos;
    private JLabel lblProbQuiebra;
    private JLabel lblPromPartidas;
    private JLabel lblPromLanzamientos;

    private JSpinner spnCorridasEstimacion;
    private JComboBox<String> cmbVelocidad;

    private JTextArea txtBitacora;
    private Timer timerAutomatico;
    private JButton btnPaso;
    private JButton btnAutomatico;
    private JButton btnDetener;
    private JButton btnNuevaCorrida;
    private JButton btnEstimar;
    private JButton btnReiniciarEstadisticas;

    private JProgressBar barraCapital;
    private CapitalChartPanel panelGrafica;

    public JuegoDadosUI() {
        super("Aplicación 1 - Juego 7-11 | Simulación de quiebra");
        configurarVentana();
        inicializarComponentes();
        configurarEventos();
        crearTimer();
        iniciarNuevaCorridaVisual();
    }

    private void configurarVentana() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));
    }

    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(12, 12));
        panelPrincipal.setBorder(new EmptyBorder(12, 12, 12, 12));
        panelPrincipal.setBackground(new Color(245, 247, 250));
        setContentPane(panelPrincipal);

        panelPrincipal.add(crearEncabezado(), BorderLayout.NORTH);
        panelPrincipal.add(crearCentro(), BorderLayout.CENTER);
        panelPrincipal.add(crearResumenGlobal(), BorderLayout.SOUTH);
    }

    private JPanel crearEncabezado() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JLabel titulo = new JLabel("Aplicación 1: Juego 7-11", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 28));

        JLabel subtitulo = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "Simulación Monte Carlo de la probabilidad de quiebra — "
                        + "Capital inicial: Bs. " + SimuladorJuego.CAPITAL_INICIAL
                        + " | Meta: Bs. " + SimuladorJuego.META_CAPITAL
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
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 205)),
                "Controles de simulación",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13)
        ));

        spnCorridasEstimacion = new JSpinner(new SpinnerNumberModel(1000, 10, 500000, 10));
        cmbVelocidad = new JComboBox<>(new String[]{"Lenta", "Media", "Rápida"});

        btnNuevaCorrida = crearBoton("Nueva corrida visual", new Color(21, 101, 192));
        btnPaso = crearBoton("Un lanzamiento", new Color(27, 94, 32));
        btnAutomatico = crearBoton("Simulación instantánea", new Color(106, 27, 154));
        btnDetener = crearBoton("Detener", new Color(183, 28, 28));
        btnEstimar = crearBoton("Estimar probabilidad", new Color(230, 81, 0));
        btnReiniciarEstadisticas = crearBoton("Reiniciar estadísticas", new Color(69, 90, 100));

        JPanel panelParametros = new JPanel(new GridBagLayout());
        panelParametros.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;

        gbc.gridx = 0; panelParametros.add(new JLabel("Velocidad:"), gbc);
        gbc.gridx = 1; panelParametros.add(cmbVelocidad, gbc);
        gbc.gridx = 2; panelParametros.add(new JLabel("Corridas para estimación:"), gbc);
        gbc.gridx = 3; panelParametros.add(spnCorridasEstimacion, gbc);

        JPanel panelBotones = new JPanel(new GridLayout(2, 3, 10, 10));
        panelBotones.setOpaque(false);
        panelBotones.setBorder(new EmptyBorder(2, 8, 2, 8));
        panelBotones.add(btnNuevaCorrida);
        panelBotones.add(btnPaso);
        panelBotones.add(btnAutomatico);
        panelBotones.add(btnDetener);
        panelBotones.add(btnEstimar);
        panelBotones.add(btnReiniciarEstadisticas);

        panel.add(panelParametros, BorderLayout.NORTH);
        panel.add(panelBotones, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearCentro() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 12, 12));
        panel.setOpaque(false);
        panel.add(crearPanelEstadoActual());
        panel.add(crearPanelVisual());
        panel.add(crearPanelBitacora());
        return panel;
    }

    private JPanel crearPanelEstadoActual() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(crearBorde("Estado actual de la corrida"));

        JPanel contenido = new JPanel(new GridLayout(8, 1, 6, 6));
        contenido.setOpaque(false);
        contenido.setBorder(new EmptyBorder(10, 10, 10, 10));

        lblCapitalActual = crearEtiquetaInfo("Capital actual: Bs. " + SimuladorJuego.CAPITAL_INICIAL);
        lblPartidaActual = crearEtiquetaInfo("Partida actual: 1");
        lblLanzamientoActual = crearEtiquetaInfo("Lanzamiento en partida: 0");
        lblEstadoPartida = crearEtiquetaInfo("Estado: Esperando lanzamiento");
        lblPuntoActual = crearEtiquetaInfo("Punto actual: -");
        lblDados = crearEtiquetaInfo("Dados: ⚀ ⚀");
        lblSuma = crearEtiquetaInfo("Suma: 0");
        lblMensajePrincipal = crearEtiquetaInfo("Mensaje: Corrida inicializada");

        contenido.add(lblCapitalActual);
        contenido.add(lblPartidaActual);
        contenido.add(lblLanzamientoActual);
        contenido.add(lblEstadoPartida);
        contenido.add(lblPuntoActual);
        contenido.add(lblDados);
        contenido.add(lblSuma);
        contenido.add(lblMensajePrincipal);

        barraCapital = new JProgressBar();
        barraCapital.setStringPainted(true);
        barraCapital.setFont(new Font("SansSerif", Font.BOLD, 13));
        barraCapital.setPreferredSize(new Dimension(100, 28));

        JPanel panelBarra = new JPanel(new BorderLayout());
        panelBarra.setOpaque(false);
        panelBarra.setBorder(new EmptyBorder(0, 10, 10, 10));
        JLabel lblBarra = new JLabel("Evolución del capital");
        lblBarra.setFont(new Font("SansSerif", Font.BOLD, 14));
        panelBarra.add(lblBarra, BorderLayout.NORTH);
        panelBarra.add(barraCapital, BorderLayout.CENTER);

        panel.add(contenido, BorderLayout.CENTER);
        panel.add(panelBarra, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelVisual() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(crearBorde("Visualización"));

        panelGrafica = new CapitalChartPanel();
        panelGrafica.setPreferredSize(new Dimension(420, 420));

        JLabel ayuda = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "<b>Lectura visual</b><br>"
                        + "La línea muestra cómo sube o baja el capital del jugador<br>"
                        + "después de cada partida ganada o perdida."
                        + "</div></html>",
                SwingConstants.CENTER
        );
        ayuda.setFont(new Font("SansSerif", Font.PLAIN, 13));
        ayuda.setBorder(new EmptyBorder(8, 8, 8, 8));

        panel.add(panelGrafica, BorderLayout.CENTER);
        panel.add(ayuda, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelBitacora() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(crearBorde("Bitácora de la simulación"));

        txtBitacora = new JTextArea();
        txtBitacora.setEditable(false);
        txtBitacora.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtBitacora.setLineWrap(true);
        txtBitacora.setWrapStyleWord(true);

        panel.add(new JScrollPane(txtBitacora), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearResumenGlobal() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 12, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(crearBorde("Resumen global"));

        lblTotalCorridas = crearEtiquetaResumen("Corridas totales: 0");
        lblTotalQuiebras = crearEtiquetaResumen("Quiebras: 0");
        lblTotalExitos = crearEtiquetaResumen("Éxitos: 0");
        lblProbQuiebra = crearEtiquetaResumen("Probabilidad de quiebra: 0.0000");
        lblPromPartidas = crearEtiquetaResumen("Promedio de partidas: 0.00");
        lblPromLanzamientos = crearEtiquetaResumen("Promedio de lanzamientos: 0.00");

        panel.add(lblTotalCorridas);
        panel.add(lblTotalQuiebras);
        panel.add(lblTotalExitos);
        panel.add(lblProbQuiebra);
        panel.add(lblPromPartidas);
        panel.add(lblPromLanzamientos);
        return panel;
    }

    // -------------------------------------------------------------------------
    // Eventos de botones
    // -------------------------------------------------------------------------

    private void configurarEventos() {
        btnNuevaCorrida.addActionListener(e -> iniciarNuevaCorridaVisual());

        btnPaso.addActionListener(e -> {
            if (!sim.activa) iniciarNuevaCorridaVisual();
            ejecutarUnLanzamiento();
        });

        btnAutomatico.addActionListener(e -> {
            if (!sim.activa) iniciarNuevaCorridaVisual();
            ejecutarCorridaInstantanea();
        });

        btnDetener.addActionListener(e -> {
            timerAutomatico.stop();
            escribirBitacora("Simulación detenida.");
        });

        btnEstimar.addActionListener(this::estimarProbabilidad);

        btnReiniciarEstadisticas.addActionListener(e -> {
            timerAutomatico.stop();
            sim.reiniciarEstadisticas();
            actualizarResumenGlobal();
            escribirBitacora("Estadísticas globales reiniciadas.");
        });
    }

    private void crearTimer() {
        timerAutomatico = new Timer(450, e -> ejecutarUnLanzamiento());
    }

    // -------------------------------------------------------------------------
    // Control de simulación
    // -------------------------------------------------------------------------

    private void iniciarNuevaCorridaVisual() {
        timerAutomatico.stop();
        sim.nuevaCorrida();

        txtBitacora.setText("");
        escribirBitacora("Nueva corrida visual iniciada.");
        escribirBitacora("Capital inicial: Bs. " + SimuladorJuego.CAPITAL_INICIAL
                + " | Meta: Bs. " + SimuladorJuego.META_CAPITAL);

        actualizarPantalla("Corrida lista para comenzar.");
        panelGrafica.repaint();
    }

    private void ejecutarCorridaInstantanea() {
        timerAutomatico.stop();
        escribirBitacora("Ejecutando corrida visual completa de forma instantánea...");
        while (sim.activa) {
            ejecutarUnLanzamiento();
        }
        escribirBitacora("La simulación instantánea finalizó con capital Bs. " + sim.capitalActual + ".");
    }

    private void ejecutarUnLanzamiento() {
        if (!sim.activa) {
            timerAutomatico.stop();
            return;
        }
        if (sim.corridaTerminada()) {
            finalizarCorrida();
            timerAutomatico.stop();
            return;
        }

        SimuladorJuego.EventoLanzamiento evento = sim.ejecutarLanzamiento();

        escribirBitacora("Partida " + evento.partida + ", lanzamiento " + evento.lanzamiento
                + " -> Dados: " + evento.dado1 + " y " + evento.dado2
                + " | Suma = " + evento.sumaDados);
        for (String msg : evento.mensajes) {
            escribirBitacora(msg);
        }

        if (evento.terminada) {
            finalizarCorrida();
            timerAutomatico.stop();
        }

        actualizarPantalla("Último evento procesado.");
        panelGrafica.repaint();
    }

    private void finalizarCorrida() {
        if (sim.contabilizada) return;

        SimuladorJuego.ResultadoCorrida resultado = sim.finalizarCorrida();
        sim.registrarCorrida(resultado);

        if (resultado.quiebra) {
            escribirBitacora("La corrida terminó en QUIEBRA.");
            actualizarPantalla("La corrida terminó en quiebra.");
        } else {
            escribirBitacora("La corrida terminó en ÉXITO al alcanzar la meta.");
            actualizarPantalla("La corrida terminó con éxito.");
        }
        actualizarResumenGlobal();
    }

    private void estimarProbabilidad(ActionEvent e) {
        timerAutomatico.stop();
        int corridas = (Integer) spnCorridasEstimacion.getValue();

        bloquearControles(true);
        escribirBitacora("Iniciando estimación rápida con " + corridas + " corridas...");

        SwingWorker<SimuladorJuego.ResultadoLote, Integer> worker = new SwingWorker<>() {
            @Override
            protected SimuladorJuego.ResultadoLote doInBackground() {
                for (int i = 1; i <= corridas; i++) {
                    if (i % Math.max(1, corridas / 20) == 0) publish(i);
                }
                return sim.simularLote(corridas);
            }

            @Override
            protected void process(List<Integer> chunks) {
                escribirBitacora("Progreso: " + chunks.get(chunks.size() - 1) + " corridas procesadas.");
            }

            @Override
            protected void done() {
                try {
                    SimuladorJuego.ResultadoLote resultado = get();
                    sim.acumularLote(resultado);
                    actualizarResumenGlobal();

                    double probLote = (double) resultado.quiebras / resultado.corridas;
                    double capitalPromedio = (double) resultado.capitalFinalAcumulado / resultado.corridas;

                    escribirBitacora("Estimación terminada.");
                    escribirBitacora("Quiebras en el lote: " + resultado.quiebras);
                    escribirBitacora("Éxitos en el lote: " + resultado.exitos);
                    escribirBitacora("Probabilidad estimada de quiebra: " + df.format(probLote));
                    escribirBitacora("Capital final promedio: Bs. " + String.format("%.2f", capitalPromedio));
                    escribirBitacora("(Bs. 0 si quiebra, Bs. " + SimuladorJuego.META_CAPITAL + " si alcanza meta)");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(JuegoDadosUI.this,
                            "Error durante la estimación:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    bloquearControles(false);
                }
            }
        };

        worker.execute();
    }

    // -------------------------------------------------------------------------
    // Actualización de pantalla — solo lee de sim, no calcula nada
    // -------------------------------------------------------------------------

    private void actualizarPantalla(String mensaje) {
        lblCapitalActual.setText("Capital actual: Bs. " + sim.capitalActual);
        lblPartidaActual.setText("Partida actual: " + sim.partidaActual);
        lblLanzamientoActual.setText("Lanzamiento en partida: " + sim.lanzamientoActual);
        lblEstadoPartida.setText("Estado: " + (sim.enFasePunto
                ? "Buscando punto o 7" : "Primer lanzamiento / nueva partida"));
        lblPuntoActual.setText("Punto actual: " + (sim.puntoActual == -1 ? "-" : sim.puntoActual));
        lblDados.setText("Dados: " + dadoUnicode(sim.dado1) + " " + dadoUnicode(sim.dado2));
        lblSuma.setText("Suma: " + sim.sumaDados);
        lblMensajePrincipal.setText("Mensaje: " + mensaje);

        barraCapital.setMinimum(0);
        barraCapital.setMaximum(SimuladorJuego.META_CAPITAL);
        barraCapital.setValue(Math.max(0, Math.min(sim.capitalActual, SimuladorJuego.META_CAPITAL)));
        barraCapital.setString("Bs. " + sim.capitalActual + " de " + SimuladorJuego.META_CAPITAL);
    }

    private void actualizarResumenGlobal() {
        lblTotalCorridas.setText("Corridas totales: " + sim.totalCorridas);
        lblTotalQuiebras.setText("Quiebras: " + sim.totalQuiebras);
        lblTotalExitos.setText("Éxitos: " + sim.totalExitos);
        lblProbQuiebra.setText("Probabilidad de quiebra: " + df.format(sim.probabilidadQuiebra()));
        lblPromPartidas.setText("Promedio de partidas: " + String.format("%.2f", sim.promedioPartidas()));
        lblPromLanzamientos.setText("Promedio de lanzamientos: " + String.format("%.2f", sim.promedioLanzamientos()));
    }

    private void escribirBitacora(String texto) {
        txtBitacora.append(texto + "\n");
        txtBitacora.setCaretPosition(txtBitacora.getDocument().getLength());
    }

    private void bloquearControles(boolean bloqueado) {
        btnNuevaCorrida.setEnabled(!bloqueado);
        btnPaso.setEnabled(!bloqueado);
        btnAutomatico.setEnabled(!bloqueado);
        btnDetener.setEnabled(!bloqueado);
        btnEstimar.setEnabled(!bloqueado);
        btnReiniciarEstadisticas.setEnabled(!bloqueado);
        spnCorridasEstimacion.setEnabled(!bloqueado);
        cmbVelocidad.setEnabled(!bloqueado);
    }

    // -------------------------------------------------------------------------
    // Helpers visuales — formato y estilo, cero lógica de juego
    // -------------------------------------------------------------------------

    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFocusPainted(false);
        boton.setForeground(Color.WHITE);
        boton.setBackground(color);
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBorderPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                new EmptyBorder(10, 14, 10, 14)
        ));
        boton.setPreferredSize(new Dimension(190, 42));
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
                titulo, TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13)
        );
    }

    private String dadoUnicode(int valor) {
        return switch (valor) {
            case 1 -> "⚀"; case 2 -> "⚁"; case 3 -> "⚂";
            case 4 -> "⚃"; case 5 -> "⚄"; case 6 -> "⚅";
            default -> "-";
        };
    }

    // -------------------------------------------------------------------------
    // Gráfica — solo pinta, lee historialCapital de sim
    // -------------------------------------------------------------------------

    private class CapitalChartPanel extends JPanel {
        public CapitalChartPanel() { setBackground(Color.WHITE); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight(), margen = 45;

            g2.setColor(new Color(230, 233, 238));
            g2.fillRoundRect(10, 10, w - 20, h - 20, 18, 18);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(20, 20, w - 40, h - 40, 18, 18);

            int x0 = margen, y0 = h - margen, xMax = w - margen, yMax = margen;

            g2.setColor(Color.DARK_GRAY);
            g2.drawLine(x0, y0, xMax, y0);
            g2.drawLine(x0, y0, x0, yMax);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.drawString("Partidas", xMax - 45, y0 + 28);
            g2.drawString("Capital", 10, yMax - 8);

            for (int i = 0; i <= 5; i++) {
                int ref = (int) Math.round((double) SimuladorJuego.META_CAPITAL * i / 5);
                int y = y0 - (int) ((double) ref / SimuladorJuego.META_CAPITAL * (y0 - yMax));
                g2.setColor(new Color(235, 238, 242));
                g2.drawLine(x0, y, xMax, y);
                g2.setColor(Color.GRAY);
                g2.drawString(String.valueOf(ref), x0 - 35, y + 5);
            }

            if (sim.historialCapital.isEmpty()) { g2.dispose(); return; }

            int puntos = sim.historialCapital.size();
            int aw = Math.max(1, xMax - x0);
            int ah = Math.max(1, y0 - yMax);

            g2.setStroke(new BasicStroke(2.5f));
            g2.setColor(new Color(41, 128, 185));
            for (int i = 0; i < puntos - 1; i++) {
                int x1 = x0 + (int) ((double) i / Math.max(1, puntos - 1) * aw);
                int x2 = x0 + (int) ((double) (i + 1) / Math.max(1, puntos - 1) * aw);
                int y1 = y0 - (int) ((double) sim.historialCapital.get(i) / SimuladorJuego.META_CAPITAL * ah);
                int y2 = y0 - (int) ((double) sim.historialCapital.get(i + 1) / SimuladorJuego.META_CAPITAL * ah);
                g2.drawLine(x1, y1, x2, y2);
            }

            g2.setColor(new Color(231, 76, 60));
            for (int i = 0; i < puntos; i++) {
                int x = x0 + (int) ((double) i / Math.max(1, puntos - 1) * aw);
                int y = y0 - (int) ((double) sim.historialCapital.get(i) / SimuladorJuego.META_CAPITAL * ah);
                g2.fillOval(x - 4, y - 4, 8, 8);
            }

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JuegoDadosUI().setVisible(true));
    }
}
