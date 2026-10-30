import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;

public class TransactionHistoryDialog extends JDialog {

    public TransactionHistoryDialog(JFrame parent, int accountId, String ownerName) {
        super(parent, "Teller — Transaction History: " + ownerName, true);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        String[] columns = {"Type", "Amount", "Date / Time"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);

        NumberFormat currency = NumberFormat.getCurrencyInstance();

        for (Object[] row : DBHelper.getTransactions(accountId)) {
            String type = (String) row[0];
            double amount = (double) row[1];
            String timestamp = (String) row[2];
            tableModel.addRow(new Object[]{type, currency.format(amount), timestamp});
        }

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        JButton closeButton = new JButton("Close");
        closeButton.setPreferredSize(new Dimension(100, 38));
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        closeButton.addActionListener((ActionEvent e) -> dispose());

        pack();
        setMinimumSize(new Dimension(getWidth(), getHeight()));
        setLocationRelativeTo(parent);
    }
}
