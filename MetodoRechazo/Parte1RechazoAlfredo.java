import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Parte1RechazoAlfredo extends JFrame {

    private static final DecimalFormat DF = new DecimalFormat("0.00");

    private JComboBox<String> comboEjercicio;
    private JComboBox<Integer> comboIteraciones;
    private JButton btnSimular;
    private JButton btnEjemplo;
    private JButton btnLimpiar;
    private JButton btnSiguientePaso;

    private JTable tabla;
    private DefaultTableModel modelo;
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

    private void initComponents() {
        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(crearPanelCentral(), BorderLayout.CENTER);
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
        lblAceptados = new JLabel("Aceptados: 0");
        lblRechazados = new JLabel("Rechazados: 0");
        lblPorcentaje = new JLabel("Porcentaje de aceptación: 0.00%");

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

    private void simular() {
        limpiarTabla();

        int ejercicio = getEjercicioSeleccionado();
        int n = (Integer) comboIteraciones.getSelectedItem();

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Parte1RechazoAlfredo::new);
    }
}
