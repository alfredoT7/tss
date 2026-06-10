import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class ResultadoRenderer extends DefaultTableCellRenderer {

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
