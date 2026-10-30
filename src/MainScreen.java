import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class MainScreen extends JFrame {

    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton deleteButton;
    private JButton depositButton;
    private JButton withdrawButton;
    private JButton historyButton;
    private JButton undoButton;
    private JLabel totalAccountsLabel;
    private JLabel totalBalanceLabel;
    private JLabel below50Label;

    public MainScreen() {
        setTitle("Teller — Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String[] columns = {"ID", "Owner Name", "Account Number", "Balance", "Type"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);

        table.setRowHeight(32);
        table.putClientProperty("Table.alternateRowColor", true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        table.setAutoCreateRowSorter(true);

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

        // North panel: stats + search
        JPanel northPanel = new JPanel(new BorderLayout(10, 10));
        northPanel.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        totalAccountsLabel = statCard(statsPanel, "Total Accounts", "0");
        totalBalanceLabel = statCard(statsPanel, "Total Balance", "$0.00");
        below50Label = statCard(statsPanel, "Below $50", "0");
        below50Label.setForeground(new Color(0xF57C00));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        searchPanel.add(new JLabel("Search:  "));
        JTextField searchField = new JTextField(20);
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search owner or account number");
        searchPanel.add(searchField);
        northPanel.add(statsPanel, BorderLayout.WEST);
        northPanel.add(searchPanel, BorderLayout.EAST);

        add(northPanel, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(760, 300));
        add(scrollPane, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(searchField); }
            public void removeUpdate(DocumentEvent e) { filter(searchField); }
            public void changedUpdate(DocumentEvent e) { filter(searchField); }
        });

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // South panel: status + button rows
        JPanel southPanel = new JPanel(new BorderLayout(0, 8));
        southPanel.setBorder(new EmptyBorder(8, 14, 12, 14));

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 8));

        JButton addButton = styledButton("Add Account");
        deleteButton = styledButton("Delete Account");
        styleButtonAccent(deleteButton, new Color(0xC62828));
        depositButton = styledButton("Deposit");
        styleButtonAccent(depositButton, new Color(0x2E7D32));
        withdrawButton = styledButton("Withdraw");
        styleButtonAccent(withdrawButton, new Color(0xC62828));
        historyButton = styledButton("View History");
        undoButton = styledButton("Undo Last Txn");
        JButton interestButton = styledButton("Apply Interest");
        JButton exportButton = styledButton("Export CSV");
        JButton refreshButton = styledButton("Refresh");
        JButton logoutButton = styledButton("Logout");

        deleteButton.setEnabled(false);
        depositButton.setEnabled(false);
        withdrawButton.setEnabled(false);
        historyButton.setEnabled(false);
        undoButton.setEnabled(false);

        table.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = table.getSelectedRow() >= 0;
            deleteButton.setEnabled(rowSelected);
            depositButton.setEnabled(rowSelected);
            withdrawButton.setEnabled(rowSelected);
            historyButton.setEnabled(rowSelected);
            undoButton.setEnabled(rowSelected);
        });

        JLabel statusLabel = new JLabel("● Database connected");
        statusLabel.setForeground(new Color(0x2E7D32));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 11f));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusPanel.add(statusLabel);

        buttonPanel.add(buttonRow(addButton, deleteButton, depositButton, withdrawButton));
        buttonPanel.add(buttonRow(historyButton, undoButton, interestButton, exportButton));
        buttonPanel.add(buttonRow(refreshButton, logoutButton));

        southPanel.add(statusPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener((ActionEvent e) -> refreshAll());
        addButton.addActionListener((ActionEvent e) -> addAccount());
        deleteButton.addActionListener((ActionEvent e) -> deleteAccount());
        depositButton.addActionListener((ActionEvent e) -> openTransactionDialog("Deposit"));
        withdrawButton.addActionListener((ActionEvent e) -> openTransactionDialog("Withdraw"));
        historyButton.addActionListener((ActionEvent e) -> viewHistory());
        undoButton.addActionListener((ActionEvent e) -> undoLastTransaction());
        interestButton.addActionListener((ActionEvent e) -> applyInterest());
        exportButton.addActionListener((ActionEvent e) -> exportCSV());
        logoutButton.addActionListener((ActionEvent e) -> {
            dispose();
            LoginScreen loginScreen = new LoginScreen();
            loginScreen.setVisible(true);
        });

        pack();
        int minWidth = Math.max(getWidth(),
                northPanel.getPreferredSize().width + getInsets().left + getInsets().right + 20);
        setSize(minWidth, getHeight());
        setMinimumSize(new Dimension(minWidth, getHeight()));
        setLocationRelativeTo(null);
        refreshAll();
    }

    private JPanel buttonRow(JButton... buttons) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        for (JButton button : buttons) {
            row.add(button);
        }
        return row;
    }

    private JLabel statCard(JPanel statsPanel, String label, String value) {
        JPanel card = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xCCCCCC), 1),
                new EmptyBorder(8, 16, 8, 16)));
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(labelComp.getFont().deriveFont(Font.PLAIN, 11f));
        labelComp.setForeground(new Color(0x666666));
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 16f));
        card.add(labelComp);
        card.add(valueLabel);
        statsPanel.add(card);
        return valueLabel;
    }

    private void filter(JTextField searchField) {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1, 2));
        }
    }

    private void refreshAll() {
        loadAccounts();
        updateStats();
    }

    private void loadAccounts() {
        TableRowSorter<DefaultTableModel> currentSorter = sorter;
        table.setRowSorter(null);
        tableModel.setRowCount(0);
        List<Object[]> accounts = DBHelper.getAllAccounts();
        for (Object[] row : accounts) {
            tableModel.addRow(row);
        }
        table.setRowSorter(currentSorter);
    }

    private void updateStats() {
        Object[] stats = DBHelper.getStats();
        int totalAccounts = (int) stats[0];
        double totalBalance = (double) stats[1];
        int below50 = (int) stats[2];
        totalAccountsLabel.setText(String.valueOf(totalAccounts));
        totalBalanceLabel.setText(NumberFormat.getCurrencyInstance().format(totalBalance));
        below50Label.setText(String.valueOf(below50));
    }

    private JButton styledButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(140, 40));
        return button;
    }

    private void styleButtonAccent(JButton button, Color color) {
        button.setForeground(color);
    }

    private void addAccount() {
        JTextField nameField = new JTextField(15);
        JTextField balanceField = new JTextField(10);
        String[] types = {"SAVINGS", "CHECKING"};
        JComboBox<String> typeCombo = new JComboBox<>(types);

        JPanel panel = new JPanel(new GridLayout(3, 2, 6, 6));
        panel.add(new JLabel("Owner name:"));
        panel.add(nameField);
        panel.add(new JLabel("Initial balance:"));
        panel.add(balanceField);
        panel.add(new JLabel("Account type:"));
        panel.add(typeCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Account",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String ownerName = nameField.getText().trim();
        if (ownerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Owner name is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String balanceStr = balanceField.getText().trim();
        if (balanceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Initial balance is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double balance = Double.parseDouble(balanceStr);
            if (balance < 0) {
                JOptionPane.showMessageDialog(this, "Initial balance cannot be negative.", "Invalid Balance", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String accountNumber = "ACC" + System.currentTimeMillis();
            String accountType = (String) typeCombo.getSelectedItem();
            if (DBHelper.addAccount(ownerName, accountNumber, balance, accountType)) {
                refreshAll();
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
        int id = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
        String ownerName = (String) tableModel.getValueAt(table.convertRowIndexToModel(row), 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete account for \"" + ownerName + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        if (DBHelper.deleteAccount(id)) {
            refreshAll();
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
        int id = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
        String ownerName = (String) tableModel.getValueAt(table.convertRowIndexToModel(row), 1);
        double balance = (double) tableModel.getValueAt(table.convertRowIndexToModel(row), 3);

        TransactionDialog dialog = new TransactionDialog(this, "Teller — " + type + " Account", ownerName, balance);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            double amount = dialog.getAmount();
            boolean success;
            String currencyStr = NumberFormat.getCurrencyInstance().format(amount);
            if ("Deposit".equals(dialog.getTransactionType())) {
                success = DBHelper.deposit(id, amount);
                if (success) {
                    refreshAll();
                    JOptionPane.showMessageDialog(this,
                            "Deposited " + currencyStr + ".",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Deposit failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                success = DBHelper.withdraw(id, amount);
                if (success) {
                    refreshAll();
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

    private void viewHistory() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        int id = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
        String ownerName = (String) tableModel.getValueAt(table.convertRowIndexToModel(row), 1);
        TransactionHistoryDialog dialog = new TransactionHistoryDialog(this, id, ownerName);
        dialog.setVisible(true);
        refreshAll();
    }

    private void undoLastTransaction() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        int id = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
        String ownerName = (String) tableModel.getValueAt(table.convertRowIndexToModel(row), 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Undo last transaction for \"" + ownerName + "\"?",
                "Confirm Undo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        if (DBHelper.undoLastTransaction(id)) {
            refreshAll();
            JOptionPane.showMessageDialog(this, "Transaction reversed.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No transaction to undo.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyInterest() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Apply 2% interest to all SAVINGS accounts?",
                "Confirm Interest", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        if (DBHelper.applyInterest()) {
            refreshAll();
            JOptionPane.showMessageDialog(this, "Interest applied.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to apply interest.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export to CSV");
        chooser.setSelectedFile(new File("accounts.csv"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("ID,Owner Name,Account Number,Balance,Type\n");
            int rowCount = table.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                writer.write(tableModel.getValueAt(table.convertRowIndexToModel(i), 0) + ",");
                writer.write(tableModel.getValueAt(table.convertRowIndexToModel(i), 1) + ",");
                writer.write(tableModel.getValueAt(table.convertRowIndexToModel(i), 2) + ",");
                writer.write(tableModel.getValueAt(table.convertRowIndexToModel(i), 3) + ",");
                writer.write(tableModel.getValueAt(table.convertRowIndexToModel(i), 4) + "\n");
            }
            JOptionPane.showMessageDialog(this, "Exported to " + file.getName(), "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Export failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
