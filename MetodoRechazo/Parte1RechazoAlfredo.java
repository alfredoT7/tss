import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Parte1RechazoAlfredo extends JFrame {

    private static final DecimalFormat DF = new DecimalFormat("0.00");

    private JComboBox<String>  comboEjercicio;
    private JComboBox<Integer> comboIteraciones;
    private JButton btnSimular;
    private JButton btnEjemplo;
    private JButton btnLimpiar;
    private JButton btnSiguientePaso;

    private JTable             tabla;
    private DefaultTableModel  modelo;
    private GraficaRechazoPanel panelGrafica;
    private JTextArea areaProceso;

    private JLabel lblTitulo;
    private JLabel lblSubtitulo;
    private JLabel lblIteraciones;
    private JLabel lblAceptados;
    private JLabel lblRechazados;
    private JLabel lblPorcentaje;

    private final Random random = new Random();
    private final List<String> pasosProcesoActual = new ArrayList<>();
    private int indicePasoActual = 0;

    public Parte1RechazoAlfredo() {
        setTitle("Parte 1 - Método del Rechazo");
        setSize(1180, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        configurarSegunEjercicio();
        setVisible(true);
    }

    // =========================================================================
    // Construcción de la interfaz
    // =========================================================================

    private void initComponents() {
        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(crearPanelCentral(),  BorderLayout.CENTER);
        add(crearPanelInferior(), BorderLayout.SOUTH);

        comboEjercicio.addActionListener(e -> configurarSegunEjercicio());
        btnSimular.addActionListener(e -> simular());
        btnEjemplo.addActionListener(e -> cargarEjemplo());
        btnSiguientePaso.addActionListener(e -> avanzarPasoProceso());
        btnLimpiar.addActionListener(e -> limpiarTabla());
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        lblTitulo = new JLabel("Parte 1 - Método del Rechazo", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        panel.add(lblTitulo, BorderLayout.NORTH);

        lblSubtitulo = new JLabel("", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(lblSubtitulo, BorderLayout.CENTER);

        panel.add(crearPanelControles(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelControles() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        panel.add(new JLabel("Ejercicio:"));
        comboEjercicio = new JComboBox<>(new String[]{"Ejercicio 1", "Ejercicio 2"});
        panel.add(comboEjercicio);

        panel.add(new JLabel("Número de iteraciones:"));
        comboIteraciones = new JComboBox<>(new Integer[]{100, 200, 500, 1000});
        comboIteraciones.setSelectedItem(100);
        panel.add(comboIteraciones);

        btnSimular = new JButton("Simular");
        btnEjemplo = new JButton("Cargar ejemplo");
        btnLimpiar = new JButton("Limpiar");
        btnSiguientePaso = new JButton("Siguiente paso");

        panel.add(btnSimular);
        panel.add(btnEjemplo);
        panel.add(btnSiguientePaso);
        panel.add(btnLimpiar);
        return panel;
    }

    private JSplitPane crearPanelCentral() {
        modelo = new DefaultTableModel(columnasEjercicio1(), 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(26);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        tabla.setDefaultRenderer(Object.class, new ResultadoRenderer());

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Tabla de simulación"));

        areaProceso = new JTextArea();
        areaProceso.setEditable(false);
        areaProceso.setLineWrap(true);
        areaProceso.setWrapStyleWord(true);
        areaProceso.setMargin(new Insets(10, 10, 10, 10));
        areaProceso.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JScrollPane scrollProceso = new JScrollPane(areaProceso);
        scrollProceso.setBorder(BorderFactory.createTitledBorder("Desarrollo paso a paso"));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Tabla de simulación", scroll);
        tabs.addTab("Proceso paso a paso", scrollProceso);

        panelGrafica = new GraficaRechazoPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabs, panelGrafica);
        split.setResizeWeight(0.68);
        return split;
    }

    private JPanel crearPanelInferior() {
        JPanel panelResumen = new JPanel(new GridLayout(2, 2, 15, 10));
        panelResumen.setBorder(BorderFactory.createTitledBorder("Resumen de resultados"));

        lblIteraciones = new JLabel("Iteraciones: 0");
        lblAceptados   = new JLabel("Aceptados: 0");
        lblRechazados  = new JLabel("Rechazados: 0");
        lblPorcentaje  = new JLabel("Porcentaje de aceptación: 0.00%");

        Font f = new Font("SansSerif", Font.BOLD, 14);
        lblIteraciones.setFont(f);
        lblAceptados.setFont(f);
        lblRechazados.setFont(f);
        lblPorcentaje.setFont(f);

        panelResumen.add(lblIteraciones);
        panelResumen.add(lblAceptados);
        panelResumen.add(lblRechazados);
        panelResumen.add(lblPorcentaje);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        panel.add(panelResumen, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // Columnas de la tabla
    // =========================================================================

    private String[] columnasEjercicio1() {
        return new String[]{
            "Iteración", "R1", "R2", "x candidato", "Función", "f(x)",
            "C", "Límite = f(x) * C", "Comparación", "Resultado"
        };
    }

    private String[] columnasEjercicio2() {
        return new String[]{
            "Iteración", "R1", "R2", "x candidato", "Función", "f(x)",
            "C", "Límite = f(x) * C", "Comparación", "Resultado"
        };
    }

    // =========================================================================
    // Configuración según ejercicio seleccionado
    // =========================================================================

    private void configurarSegunEjercicio() {
        limpiarTabla();

        if (getEjercicioSeleccionado() == 1) {
            lblSubtitulo.setText(
                "<html><center>"
                + "Ejercicio 1: f1(x) = -0.5x + 2.75 para 4 <= x <= 5"
                + " &nbsp;&nbsp;&nbsp; | &nbsp;&nbsp;&nbsp;"
                + "f2(x) = 0.5x - 2.25 para 5 < x <= 6"
                + " &nbsp;&nbsp;&nbsp; | &nbsp;&nbsp;&nbsp;"
                + "C = 4/3"
                + "</center></html>"
            );
            modelo.setColumnIdentifiers(columnasEjercicio1());
        } else {
            lblSubtitulo.setText(
                "<html><center>"
                + "Ejercicio 2: f1(x) = 0.25 para 0 <= x <= 1"
                + " &nbsp;&nbsp;&nbsp; | &nbsp;&nbsp;&nbsp;"
                + "f2(x) = 3x - 2.75 para 1 < x <= 1.5"
                + " &nbsp;&nbsp;&nbsp; | &nbsp;&nbsp;&nbsp;"
                + "C = 4/7"
                + "</center></html>"
            );
            modelo.setColumnIdentifiers(columnasEjercicio2());
        }

        prepararProceso(generarPasosTeoriaMasEjemplo(getEjercicioSeleccionado()));
    }

    private int getEjercicioSeleccionado() {
        return comboEjercicio.getSelectedIndex() == 0 ? 1 : 2;
    }

    // =========================================================================
    // Acciones de botones
    // =========================================================================

    private void simular() {
        limpiarTabla();

        int ejercicio = getEjercicioSeleccionado();
        int n         = (Integer) comboIteraciones.getSelectedItem();

        RechazoLogica.ResultadoSimulacion resultado =
            RechazoLogica.simular(ejercicio, n, random);

        mostrarResultado(resultado);
    }

    private void cargarEjemplo() {
        limpiarTabla();

        int ejercicio = getEjercicioSeleccionado();
        RechazoLogica.ResultadoSimulacion resultado = crearResultadoEjemplo(ejercicio);

        mostrarResultado(resultado);
    }

    private void limpiarTabla() {
        modelo.setRowCount(0);
        actualizarResumen(0, 0, 0);
        reiniciarProceso();
        if (panelGrafica != null) {
            panelGrafica.limpiar(getEjercicioSeleccionado());
        }
    }

    // =========================================================================
    // Renderizado de resultados en la UI
    // =========================================================================

    private void mostrarResultado(RechazoLogica.ResultadoSimulacion resultado) {
        int ejercicio = getEjercicioSeleccionado();

        for (RechazoLogica.FilaSimulacion fila : resultado.filas) {
            modelo.addRow(construirFila(fila));
        }

        actualizarResumen(resultado.total(), resultado.aceptados, resultado.rechazados);
        List<String> pasos = new ArrayList<>();
        pasos.addAll(RechazoLogica.generarPasosTeoricos(ejercicio));
        pasos.addAll(RechazoLogica.generarPasosIteraciones(resultado, ejercicio, 5));
        prepararProceso(pasos);
        panelGrafica.actualizar(ejercicio, resultado.puntos);
    }

    private Object[] construirFila(RechazoLogica.FilaSimulacion fila) {
        return new Object[]{
            fila.iteracion,
            DF.format(fila.r1),
            DF.format(fila.r2),
            DF.format(fila.x),
            fila.funcion,
            DF.format(fila.fx),
            DF.format(fila.c),
            DF.format(fila.limite),
            DF.format(fila.r2) + " <= " + DF.format(fila.limite),
            fila.aceptado ? "Aceptado" : "Rechazado"
        };
    }

    private void actualizarResumen(int iteraciones, int aceptados, int rechazados) {
        double porcentaje = iteraciones == 0 ? 0.0 : (aceptados * 100.0 / iteraciones);

        lblIteraciones.setText("Iteraciones: " + iteraciones);
        lblAceptados.setText("Aceptados: " + aceptados);
        lblRechazados.setText("Rechazados: " + rechazados);
        lblPorcentaje.setText("Porcentaje de aceptación: " + DF.format(porcentaje) + "%");
    }

    private void mostrarProceso(String texto) {
        areaProceso.setText(texto);
        areaProceso.setCaretPosition(0);
    }

    private List<String> generarPasosTeoriaMasEjemplo(int ejercicio) {
        List<String> pasos = new ArrayList<>();
        pasos.addAll(RechazoLogica.generarPasosTeoricos(ejercicio));
        pasos.addAll(RechazoLogica.generarPasosIteraciones(crearResultadoEjemplo(ejercicio), ejercicio, 5));
        return pasos;
    }

    private RechazoLogica.ResultadoSimulacion crearResultadoEjemplo(int ejercicio) {
        double[] r1;
        double[] r2;

        if (ejercicio == 1) {
            r1 = new double[]{0.24, 0.02, 0.67, 0.71, 0.43};
            r2 = new double[]{0.95, 0.84, 0.19, 0.29, 0.52};
        } else {
            r1 = new double[]{0.10, 0.40, 0.75, 0.95, 0.62};
            r2 = new double[]{0.05, 0.11, 0.20, 0.66, 0.34};
        }

        return RechazoLogica.simularConDatos(ejercicio, r1, r2);
    }

    private void prepararProceso(List<String> pasos) {
        pasosProcesoActual.clear();
        pasosProcesoActual.addAll(pasos);
        indicePasoActual = 0;

        if (pasosProcesoActual.isEmpty()) {
            mostrarProceso("");
        } else {
            mostrarProceso(pasosProcesoActual.get(0));
            indicePasoActual = 1;
        }

        actualizarEstadoBotonPaso();
    }

    private void avanzarPasoProceso() {
        if (indicePasoActual >= pasosProcesoActual.size()) {
            actualizarEstadoBotonPaso();
            return;
        }

        String textoActual = areaProceso.getText();
        String siguientePaso = pasosProcesoActual.get(indicePasoActual);
        if (textoActual == null || textoActual.isEmpty()) {
            mostrarProceso(siguientePaso);
        } else {
            mostrarProceso(textoActual + "\n\n" + siguientePaso);
        }

        indicePasoActual++;
        actualizarEstadoBotonPaso();
    }

    private void reiniciarProceso() {
        pasosProcesoActual.clear();
        indicePasoActual = 0;
        if (areaProceso != null) {
            mostrarProceso("");
        }
        actualizarEstadoBotonPaso();
    }

    private void actualizarEstadoBotonPaso() {
        if (btnSiguientePaso != null) {
            btnSiguientePaso.setEnabled(indicePasoActual < pasosProcesoActual.size());
        }
    }

    // =========================================================================
    // Componentes internos de UI
    // =========================================================================

    private static class GraficaRechazoPanel extends JPanel {
        private final java.util.List<RechazoLogica.PuntoGrafica> puntos = new ArrayList<>();
        private int ejercicio = 1;

        GraficaRechazoPanel() {
            setPreferredSize(new Dimension(360, 420));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createTitledBorder("Gráfica de las simulaciones"));
        }

        void actualizar(int ejercicio, java.util.List<RechazoLogica.PuntoGrafica> nuevosPuntos) {
            this.ejercicio = ejercicio;
            puntos.clear();
            puntos.addAll(nuevosPuntos);
            repaint();
        }

        void limpiar(int ejercicio) {
            this.ejercicio = ejercicio;
            puntos.clear();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int left   = 58;
            int right  = 24;
            int top    = 42;
            int bottom = 58;
            int w = getWidth()  - left - right;
            int h = getHeight() - top  - bottom;

            if (w <= 0 || h <= 0) { g2.dispose(); return; }

            double minX = ejercicio == 1 ? 4.0 : 0.0;
            double maxX = ejercicio == 1 ? 6.0 : 1.5;

            dibujarEjes(g2, left, top, w, h, minX, maxX);
            dibujarCurvaLimite(g2, left, top, w, h, minX, maxX);
            dibujarPuntos(g2, left, top, w, h, minX, maxX);
            dibujarLeyenda(g2, left, top);

            g2.dispose();
        }

        private void dibujarEjes(Graphics2D g2, int left, int top, int w, int h,
                                  double minX, double maxX) {
            g2.setColor(new Color(245, 245, 245));
            for (int i = 0; i <= 5; i++) {
                int y = top + h - (int) Math.round(h * i / 5.0);
                g2.drawLine(left, y, left + w, y);
            }

            g2.setColor(new Color(80, 80, 80));
            g2.drawLine(left, top, left, top + h);
            g2.drawLine(left, top + h, left + w, top + h);

            DecimalFormat df = new DecimalFormat("0.00");
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));

            for (int i = 0; i <= 5; i++) {
                double valorY = i / 5.0;
                int y = top + h - (int) Math.round(h * valorY);
                g2.drawString(df.format(valorY), 18, y + 4);
            }

            for (int i = 0; i <= 4; i++) {
                double valorX = minX + (maxX - minX) * i / 4.0;
                int x = left + (int) Math.round(w * i / 4.0);
                g2.drawString(df.format(valorX), x - 12, top + h + 22);
            }

            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString("x", left + w - 5, top + h + 42);
            g2.rotate(-Math.PI / 2);
            g2.drawString("R2 / limite", -(top + 90), 15);
            g2.rotate(Math.PI / 2);
        }

        private void dibujarCurvaLimite(Graphics2D g2, int left, int top, int w, int h,
                                         double minX, double maxX) {
            g2.setColor(new Color(40, 95, 170));
            g2.setStroke(new BasicStroke(2f));

            int puntosCurva = 120;
            int prevX = -1, prevY = -1;

            for (int i = 0; i <= puntosCurva; i++) {
                double xValor = minX + (maxX - minX) * i / puntosCurva;
                double limite = RechazoLogica.calcularLimite(ejercicio, xValor);
                int x = mapX(xValor, minX, maxX, left, w);
                int y = mapY(limite, top, h);

                if (i > 0) g2.drawLine(prevX, prevY, x, y);

                prevX = x;
                prevY = y;
            }
        }

        private void dibujarPuntos(Graphics2D g2, int left, int top, int w, int h,
                                    double minX, double maxX) {
            for (RechazoLogica.PuntoGrafica punto : puntos) {
                int x = mapX(punto.x, minX, maxX, left, w);
                int y = mapY(punto.r2, top, h);

                g2.setColor(punto.aceptado ? new Color(30, 140, 70) : new Color(200, 55, 55));
                g2.fillOval(x - 4, y - 4, 8, 8);

                g2.setColor(new Color(255, 255, 255, 190));
                g2.drawOval(x - 4, y - 4, 8, 8);
            }

            if (puntos.isEmpty()) {
                g2.setColor(new Color(120, 120, 120));
                g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
                g2.drawString("Presione Simular para graficar las n simulaciones.", left + 10, top + 24);
            }
        }

        private void dibujarLeyenda(Graphics2D g2, int left, int top) {
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));

            g2.setColor(new Color(40, 95, 170));
            g2.drawLine(left, top - 16, left + 28, top - 16);
            g2.setColor(new Color(60, 60, 60));
            g2.drawString("Limite f(x) * C", left + 34, top - 12);

            g2.setColor(new Color(30, 140, 70));
            g2.fillOval(left + 145, top - 21, 9, 9);
            g2.setColor(new Color(60, 60, 60));
            g2.drawString("Aceptado", left + 160, top - 12);

            g2.setColor(new Color(200, 55, 55));
            g2.fillOval(left + 235, top - 21, 9, 9);
            g2.setColor(new Color(60, 60, 60));
            g2.drawString("Rechazado", left + 250, top - 12);
        }

        private int mapX(double valor, double minX, double maxX, int left, int w) {
            return left + (int) Math.round((valor - minX) * w / (maxX - minX));
        }

        private int mapY(double valor, int top, int h) {
            return top + h - (int) Math.round(valor * h);
        }
    }

    private class ResultadoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                String resultado = table.getValueAt(row, 9).toString();
                c.setBackground("Aceptado".equals(resultado)
                    ? new Color(220, 255, 220)
                    : new Color(255, 220, 220));
            }

            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }

    private static class RechazoLogica {

        public static final double C_EJ1 = 4.0 / 3.0;
        public static final double C_EJ2 = 4.0 / 7.0;
        private static final DecimalFormat DF4 = new DecimalFormat("0.0000");

        public static class FilaSimulacion {
            public final int iteracion;
            public final double r1, r2, x, fx, c, limite;
            public final String funcion;
            public final boolean aceptado;

            public FilaSimulacion(int iteracion, double r1, double r2,
                                  double x, String funcion, double fx, double c,
                                  double limite, boolean aceptado) {
                this.iteracion = iteracion;
                this.r1        = r1;
                this.r2        = r2;
                this.x         = x;
                this.funcion   = funcion;
                this.fx        = fx;
                this.c         = c;
                this.limite    = limite;
                this.aceptado  = aceptado;
            }
        }

        public static class PuntoGrafica {
            public final double x, r2, limite;
            public final boolean aceptado;

            public PuntoGrafica(double x, double r2, double limite, boolean aceptado) {
                this.x        = x;
                this.r2       = r2;
                this.limite   = limite;
                this.aceptado = aceptado;
            }
        }

        public static class ResultadoSimulacion {
            public final java.util.List<FilaSimulacion> filas;
            public final java.util.List<PuntoGrafica> puntos;
            public final int aceptados;
            public final int rechazados;

            public ResultadoSimulacion(java.util.List<FilaSimulacion> filas,
                                       java.util.List<PuntoGrafica> puntos,
                                       int aceptados, int rechazados) {
                this.filas      = filas;
                this.puntos     = puntos;
                this.aceptados  = aceptados;
                this.rechazados = rechazados;
            }

            public int total() {
                return aceptados + rechazados;
            }

            public double porcentajeAceptacion() {
                return total() == 0 ? 0.0 : (aceptados * 100.0 / total());
            }
        }

        // Convierte el numero aleatorio R1 en el valor X segun el ejercicio elegido.
        public static double calcularX(int ejercicio, double r1) {
            return ejercicio == 1 ? 4 + 2 * r1 : 1.5 * r1;
        }

        // Indica si X cae en el tramo F1 o F2 de la funcion por partes.
        public static String determinarFuncion(int ejercicio, double x) {
            if (ejercicio == 1) return x <= 5.0 ? "F1" : "F2";
            return x <= 1.0 ? "F1" : "F2";
        }

        // Calcula el valor de f(x) usando la formula que corresponde al tramo.
        public static double calcularFx(int ejercicio, double x) {
            if (ejercicio == 1) return x <= 5.0 ? -0.5 * x + 2.75 : 0.5 * x - 2.25;
            return x <= 1.0 ? 0.25 : 3.0 * x - 2.75;
        }

        // Devuelve la constante C usada en el metodo de rechazo para cada ejercicio.
        public static double calcularC(int ejercicio) {
            return ejercicio == 1 ? C_EJ1 : C_EJ2;
        }

        // Obtiene el limite de aceptacion multiplicando f(x) por la constante C.
        public static double calcularLimite(int ejercicio, double x) {
            return calcularFx(ejercicio, x) * calcularC(ejercicio);
        }

        // Resuelve una iteracion completa: transforma R1, evalua f(x), calcula limite y decide aceptacion.
        public static FilaSimulacion resolverIteracion(int ejercicio, int iteracion,
                                                       double r1, double r2) {
            double x = calcularX(ejercicio, r1);
            String funcion = determinarFuncion(ejercicio, x);
            double fx = calcularFx(ejercicio, x);
            double c = calcularC(ejercicio);
            double limite = fx * c;
            boolean aceptado = r2 <= limite;

            return new FilaSimulacion(iteracion, r1, r2, x, funcion, fx, c, limite, aceptado);
        }

        // Ejecuta n iteraciones generando R1 y R2 aleatoriamente y devuelve todos los resultados.
        public static ResultadoSimulacion simular(int ejercicio, int n, Random random) {
            java.util.List<FilaSimulacion> filas = new ArrayList<>();
            java.util.List<PuntoGrafica> puntos = new ArrayList<>();
            int aceptados = 0, rechazados = 0;

            for (int i = 1; i <= n; i++) {
                double r1 = random.nextDouble();
                double r2 = random.nextDouble();

                FilaSimulacion fila = resolverIteracion(ejercicio, i, r1, r2);
                filas.add(fila);
                puntos.add(new PuntoGrafica(fila.x, fila.r2, fila.limite, fila.aceptado));

                if (fila.aceptado) aceptados++; else rechazados++;
            }

            return new ResultadoSimulacion(filas, puntos, aceptados, rechazados);
        }

        // Ejecuta la simulacion usando arreglos de datos ya dados, util para ejemplos o pruebas.
        public static ResultadoSimulacion simularConDatos(int ejercicio,
                                                          double[] r1, double[] r2) {
            java.util.List<FilaSimulacion> filas = new ArrayList<>();
            java.util.List<PuntoGrafica> puntos = new ArrayList<>();
            int aceptados = 0, rechazados = 0;

            for (int i = 0; i < r1.length; i++) {
                FilaSimulacion fila = resolverIteracion(ejercicio, i + 1, r1[i], r2[i]);
                filas.add(fila);
                puntos.add(new PuntoGrafica(fila.x, fila.r2, fila.limite, fila.aceptado));

                if (fila.aceptado) aceptados++; else rechazados++;
            }

            return new ResultadoSimulacion(filas, puntos, aceptados, rechazados);
        }

        public static List<String> generarPasosTeoricos(int ejercicio) {
            List<String> pasos = new ArrayList<>();
            pasos.add(
                "PROCESO DEL METODO DEL RECHAZO\n\n"
                + "PARTE 1: PARAMETROS Y RESOLUCION TEORICA\n\n"
                + "Ejercicio seleccionado: Ejercicio " + ejercicio
            );

            pasos.add(
                "Paso 1. Generacion de numeros aleatorios\n"
                + "Se generan dos numeros aleatorios uniformes:\n"
                + "R1 en [0,1]\n"
                + "R2 en [0,1]\n"
                + "Estos valores seran generados computacionalmente mediante random.nextDouble() o Math.random()."
            );

            if (ejercicio == 1) {
                pasos.add(
                    "Paso 2. Determinacion de la variable aleatoria x uniforme\n"
                    + "Aqui se muestra la transformacion general de R1 hacia el intervalo del ejercicio.\n\n"
                    + "Intervalo: 4 <= x <= 6\n"
                    + "x = a + (b - a)R1\n"
                    + "x = 4 + (6 - 4)R1\n"
                    + "x = 4 + 2R1"
                );

                pasos.add(
                    "Paso 3. Definicion de la funcion f(x(R1))\n"
                    + "f(x) = -0.5x + 2.75, para 4 <= x <= 5\n"
                    + "f(x) = 0.5x - 2.25, para 5 < x <= 6\n\n"
                    + "Reemplazando x(R1):\n"
                    + "Si 4 <= x <= 5:\n"
                    + "f(x(R1)) = -0.5(4 + 2R1) + 2.75\n\n"
                    + "Si 5 < x <= 6:\n"
                    + "f(x(R1)) = 0.5(4 + 2R1) - 2.25"
                );

                pasos.add(
                    "Paso 4. Determinacion de la desigualdad de aceptacion\n"
                    + "R2 <= f(x) * C\n\n"
                    + "fmax = 3/4 = 0.75\n"
                    + "C = 1 / fmax\n"
                    + "C = 1 / (3/4)\n"
                    + "C = 4/3\n\n"
                    + "Desigualdad:\n"
                    + "R2 <= f(x) * 4/3"
                );
            } else {
                pasos.add(
                    "Paso 2. Determinacion de la variable aleatoria x uniforme\n"
                    + "Aqui se muestra la transformacion general de R1 hacia el intervalo del ejercicio.\n\n"
                    + "Intervalo: 0 <= x <= 3/2\n"
                    + "x = a + (b - a)R1\n"
                    + "x = 0 + (3/2 - 0)R1\n"
                    + "x = (3/2)R1"
                );

                pasos.add(
                    "Paso 3. Definicion de la funcion f(x(R1))\n"
                    + "f(x) = 0.25, para 0 <= x <= 1\n"
                    + "f(x) = 3x - 2.75, para 1 < x <= 1.5\n\n"
                    + "Reemplazando x(R1):\n"
                    + "Si 0 <= x <= 1:\n"
                    + "f(x(R1)) = 0.25\n\n"
                    + "Si 1 < x <= 1.5:\n"
                    + "f(x(R1)) = 3((3/2)R1) - 2.75"
                );

                pasos.add(
                    "Paso 4. Determinacion de la desigualdad de aceptacion\n"
                    + "R2 <= f(x) * C\n\n"
                    + "fmax = 7/4 = 1.75\n"
                    + "C = 1 / fmax\n"
                    + "C = 1 / (7/4)\n"
                    + "C = 4/7\n\n"
                    + "Desigualdad:\n"
                    + "R2 <= f(x) * 4/7"
                );
            }

            pasos.add(
                "Paso 5. Verificacion de la desigualdad\n"
                + "Si R2 <= f(x) * C, entonces el valor x se acepta.\n"
                + "Si R2 > f(x) * C, entonces el valor x se rechaza.\n\n"
                + "Interpretacion:\n"
                + "Los valores aceptados forman la muestra simulada de la distribucion objetivo. "
                + "Los valores rechazados fueron generados como candidatos, pero no entran a la muestra final."
            );

            return pasos;
        }

        public static List<String> generarPasosIteraciones(ResultadoSimulacion resultado, int ejercicio,
                                                           int limiteIteraciones) {
            List<String> pasos = new ArrayList<>();
            pasos.add("PARTE 2: DESARROLLO OPERATIVO DE LAS ITERACIONES");

            int total = Math.min(limiteIteraciones, resultado.filas.size());
            for (int i = 0; i < total; i++) {
                FilaSimulacion fila = resultado.filas.get(i);
                StringBuilder sb = new StringBuilder();
                sb.append("ITERACION ").append(fila.iteracion).append("\n\n");
                sb.append("Paso 1. Generar R1 y R2\n");
                sb.append("R1 = ").append(DF4.format(fila.r1)).append("\n");
                sb.append("R2 = ").append(DF4.format(fila.r2)).append("\n");
                sb.append("Los valores fueron generados usando random.nextDouble() o Math.random().\n\n");

                sb.append("Paso 2. Calcular x\n");
                sb.append(describirTransformacion(ejercicio, fila.r1, fila.x)).append("\n\n");

                sb.append("Paso 3. Evaluar f(x)\n");
                sb.append(describirTramo(ejercicio, fila.x, fila.funcion)).append("\n");
                sb.append(describirFx(ejercicio, fila)).append("\n\n");

                sb.append("Paso 4. Calcular limite de aceptacion\n");
                sb.append("C = ").append(describirC(ejercicio)).append("\n");
                sb.append("Limite = f(x) * C\n");
                sb.append("Limite = ").append(DF4.format(fila.fx)).append(" * ")
                  .append(DF4.format(fila.c)).append(" = ").append(DF4.format(fila.limite)).append("\n\n");

                sb.append("Paso 5. Verificar desigualdad\n");
                sb.append("R2 <= limite\n");
                sb.append(DF4.format(fila.r2)).append(" <= ").append(DF4.format(fila.limite)).append("\n");
                sb.append("Resultado: ").append(fila.aceptado ? "Aceptado" : "Rechazado").append("\n");
                sb.append("Interpretacion: ").append(describirInterpretacion(fila.aceptado));
                pasos.add(sb.toString());
            }

            if (resultado.filas.size() > total) {
                pasos.add(
                    "Solo se muestra el desarrollo de las primeras " + total + " iteraciones.\n"
                    + "La tabla conserva todas las iteraciones generadas."
                );
            }

            return pasos;
        }

        private static String describirTransformacion(int ejercicio, double r1, double x) {
            if (ejercicio == 1) {
                return "x = 4 + 2(" + DF4.format(r1) + ") = " + DF4.format(x);
            }
            return "x = (3/2)(" + DF4.format(r1) + ") = " + DF4.format(x);
        }

        private static String describirTramo(int ejercicio, double x, String funcion) {
            if (ejercicio == 1) {
                if ("F1".equals(funcion)) {
                    return "Como x = " + DF4.format(x) + " pertenece al tramo 4 <= x <= 5, se usa F1.";
                }
                return "Como x = " + DF4.format(x) + " pertenece al tramo 5 < x <= 6, se usa F2.";
            }

            if ("F1".equals(funcion)) {
                return "Como x = " + DF4.format(x) + " pertenece al tramo 0 <= x <= 1, se usa F1.";
            }
            return "Como x = " + DF4.format(x) + " pertenece al tramo 1 < x <= 1.5, se usa F2.";
        }

        private static String describirFx(int ejercicio, FilaSimulacion fila) {
            if (ejercicio == 1) {
                if ("F1".equals(fila.funcion)) {
                    return "f(x) = -0.5(" + DF4.format(fila.x) + ") + 2.75 = " + DF4.format(fila.fx);
                }
                return "f(x) = 0.5(" + DF4.format(fila.x) + ") - 2.25 = " + DF4.format(fila.fx);
            }

            if ("F1".equals(fila.funcion)) {
                return "f(x) = 0.25";
            }
            return "f(x) = 3(" + DF4.format(fila.x) + ") - 2.75 = " + DF4.format(fila.fx);
        }

        private static String describirC(int ejercicio) {
            return ejercicio == 1 ? "4/3 = 1.3333" : "4/7 = 0.5714";
        }

        private static String describirInterpretacion(boolean aceptado) {
            if (aceptado) {
                return "el valor x entra a la muestra final porque R2 no supera el limite de aceptacion.";
            }
            return "el valor x fue generado, pero no entra a la muestra final porque R2 supera el limite de aceptacion.";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Parte1RechazoAlfredo::new);
    }
}
