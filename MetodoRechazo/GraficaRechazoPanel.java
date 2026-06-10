import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class GraficaRechazoPanel extends JPanel {

    private final List<RechazoLogica.PuntoGrafica> puntos = new ArrayList<>();
    private int ejercicio = 1;

    public GraficaRechazoPanel() {
        setPreferredSize(new Dimension(360, 420));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder("Gráfica de las simulaciones"));
    }

    public void actualizar(int ejercicio, List<RechazoLogica.PuntoGrafica> nuevosPuntos) {
        this.ejercicio = ejercicio;
        puntos.clear();
        puntos.addAll(nuevosPuntos);
        repaint();
    }

    public void limpiar(int ejercicio) {
        this.ejercicio = ejercicio;
        puntos.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int left = 58;
        int right = 24;
        int top = 42;
        int bottom = 58;
        int w = getWidth() - left - right;
        int h = getHeight() - top - bottom;

        if (w <= 0 || h <= 0) {
            g2.dispose();
            return;
        }

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
        int prevX = -1;
        int prevY = -1;

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
