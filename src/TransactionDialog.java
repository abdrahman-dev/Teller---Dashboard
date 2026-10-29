import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.AbstractAction;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;

public class TransactionDialog extends JDialog {

    private boolean confirmed = false;
    private double amount = 0;
    private String type = "Deposit";

    public TransactionDialog(JFrame parent, String title, String ownerName, double balance) {
        super(parent, title, true);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        infoPanel.add(new JLabel("Account:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        infoPanel.add(new JLabel(ownerName), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        infoPanel.add(new JLabel("Current Balance:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JLabel balanceLabel = new JLabel(NumberFormat.getCurrencyInstance().format(balance));
        if (balance <= 0) {
            balanceLabel.setForeground(new Color(0xC62828));
        } else if (balance < 50) {
            balanceLabel.setForeground(new Color(0xF57C00));
        } else {
            balanceLabel.setForeground(new Color(0x2E7D32));
        }
        infoPanel.add(balanceLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        infoPanel.add(new JLabel("Transaction Type:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Deposit", "Withdraw"});
        infoPanel.add(typeCombo, gbc);

        JPanel amountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        amountPanel.setBorder(new EmptyBorder(16, 0, 16, 0));
        amountPanel.add(new JLabel("Amount:"));
        JTextField amountField = new JTextField(14);
        amountField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "0.00");
        amountPanel.add(amountField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
        JButton okButton = new JButton("OK");
        okButton.setPreferredSize(new Dimension(100, 38));
        okButton.setForeground(new Color(0x2E7D32));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, 38));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        add(infoPanel, BorderLayout.NORTH);
        add(amountPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enter, "submitTransaction");
        getRootPane().getActionMap().put("submitTransaction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okButton.doClick();
            }
        });

        getRootPane().setDefaultButton(okButton);

        okButton.addActionListener((ActionEvent e) -> {
            String text = amountField.getText().trim();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter an amount.",
                        "Missing Amount", JOptionPane.ERROR_MESSAGE);
                amountField.requestFocusInWindow();
                return;
            }
            try {
                double amt = Double.parseDouble(text);
                if (amt <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Amount must be positive.",
                            "Invalid Amount", JOptionPane.ERROR_MESSAGE);
                    amountField.requestFocusInWindow();
                    return;
                }
                amount = amt;
                type = (String) typeCombo.getSelectedItem();
                confirmed = true;
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid number.",
                        "Invalid Amount", JOptionPane.ERROR_MESSAGE);
                amountField.requestFocusInWindow();
            }
        });

        cancelButton.addActionListener((ActionEvent e) -> dispose());

        amountField.requestFocusInWindow();

        pack();
        setMinimumSize(new Dimension(getWidth(), getHeight()));
        setLocationRelativeTo(parent);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public double getAmount() {
        return amount;
    }

    public String getTransactionType() {
        return type;
    }
}
