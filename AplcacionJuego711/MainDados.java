import javax.swing.SwingUtilities;

public class MainDados {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JuegoDadosUI().setVisible(true));
    }
}