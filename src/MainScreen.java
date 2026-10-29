import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.List;
import javax.swing.SwingConstants;

public class MainScreen extends JFrame {

    private DefaultTableModel tableModel;
    private JTable table;
    private JButton deleteButton;
    private JButton depositButton;
    private JButton withdrawButton;

    public MainScreen() {
        setTitle("Teller — Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String[] columns = {"ID", "Owner Name", "Account Number", "Balance"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);

        table.setRowHeight(34);
        table.putClientProperty("Table.alternateRowColor", true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));

        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            private final NumberFormat currency = NumberFormat.getCurrencyInstance();

            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (value instanceof Number) {
                    double bal = ((Number) value).doubleValue();
                    setText(currency.format(bal));
                    if (bal <= 0) {
                        setForeground(new Color(0xC62828));
                    } else if (bal < 50) {
                        setForeground(new Color(0xF57C00));
                    } else {
                        setForeground(new Color(0x2E7D32));
                    }
                } else {
                    setText(String.valueOf(value));
                }
                return this;
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JButton addButton = styledButton("Add Account");
        deleteButton = styledButton("Delete Account");
        styleButtonAccent(deleteButton, new Color(0xC62828));
        depositButton = styledButton("Deposit");
        styleButtonAccent(depositButton, new Color(0x2E7D32));
        withdrawButton = styledButton("Withdraw");
        styleButtonAccent(withdrawButton, new Color(0xC62828));
        JButton refreshButton = styledButton("Refresh");
        JButton logoutButton = styledButton("Logout");

        deleteButton.setEnabled(false);
        depositButton.setEnabled(false);
        withdrawButton.setEnabled(false);

        table.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = table.getSelectedRow() >= 0;
            deleteButton.setEnabled(rowSelected);
            depositButton.setEnabled(rowSelected);
            withdrawButton.setEnabled(rowSelected);
        });

        JLabel statusLabel = new JLabel("● Database connected");
        statusLabel.setForeground(new Color(0x2E7D32));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 11f));
        buttonPanel.add(statusLabel);

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(depositButton);
        buttonPanel.add(withdrawButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(logoutButton);
        add(buttonPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener((ActionEvent e) -> loadAccounts());
        addButton.addActionListener((ActionEvent e) -> addAccount());
        deleteButton.addActionListener((ActionEvent e) -> deleteAccount());
        depositButton.addActionListener((ActionEvent e) -> openTransactionDialog("Deposit"));
        withdrawButton.addActionListener((ActionEvent e) -> openTransactionDialog("Withdraw"));
        logoutButton.addActionListener((ActionEvent e) -> {
            dispose();
            LoginScreen loginScreen = new LoginScreen();
            loginScreen.setVisible(true);
        });

        pack();
        setMinimumSize(new Dimension(getWidth(), getHeight()));
        setLocationRelativeTo(null);
        loadAccounts();
    }

    private JButton styledButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(140, 40));
        return button;
    }

    private void styleButtonAccent(JButton button, Color color) {
        button.setForeground(color);
    }

    private void loadAccounts() {
        tableModel.setRowCount(0);
        List<Object[]> accounts = DBHelper.getAllAccounts();
        for (Object[] row : accounts) {
            tableModel.addRow(row);
        }
    }

    private void addAccount() {
        String ownerName = JOptionPane.showInputDialog(this, "Enter owner name:", "Add Account", JOptionPane.QUESTION_MESSAGE);
        if (ownerName == null || ownerName.trim().isEmpty()) {
            return;
        }
        String balanceStr = JOptionPane.showInputDialog(this, "Enter initial balance:", "Add Account", JOptionPane.QUESTION_MESSAGE);
        if (balanceStr == null || balanceStr.trim().isEmpty()) {
            return;
        }
        try {
            double balance = Double.parseDouble(balanceStr.trim());
            if (balance < 0) {
                JOptionPane.showMessageDialog(this, "Initial balance cannot be negative.", "Invalid Balance", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String accountNumber = "ACC" + System.currentTimeMillis();
            if (DBHelper.addAccount(ownerName.trim(), accountNumber, balance)) {
                loadAccounts();
                JOptionPane.showMessageDialog(this,
                        "Account added successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add account.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid balance amount.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteAccount() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an account to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String ownerName = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete account for \"" + ownerName + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        if (DBHelper.deleteAccount(id)) {
            loadAccounts();
            JOptionPane.showMessageDialog(this, "Account deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete account.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openTransactionDialog(String type) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an account.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String ownerName = (String) tableModel.getValueAt(row, 1);
        double balance = (double) tableModel.getValueAt(row, 3);

        TransactionDialog dialog = new TransactionDialog(this, "Teller — " + type + " Account", ownerName, balance);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            double amount = dialog.getAmount();
            boolean success;
            String currencyStr = NumberFormat.getCurrencyInstance().format(amount);
            if ("Deposit".equals(dialog.getTransactionType())) {
                success = DBHelper.deposit(id, amount);
                if (success) {
                    loadAccounts();
                    JOptionPane.showMessageDialog(this,
                            "Deposited " + currencyStr + ".",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Deposit failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                success = DBHelper.withdraw(id, amount);
                if (success) {
                    loadAccounts();
                    JOptionPane.showMessageDialog(this,
                            "Withdrew " + currencyStr + ".",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Insufficient funds.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
