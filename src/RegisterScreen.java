import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RegisterScreen extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;

    public RegisterScreen() {
        setTitle("Teller — Create Account");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(new EmptyBorder(30, 50, 30, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel title = new JLabel("Create Account");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setForeground(new Color(0x1565C0));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(title, gbc);

        JLabel hint = new JLabel("Register a new admin account");
        hint.setFont(hint.getFont().deriveFont(Font.ITALIC, 13f));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 24, 0);
        card.add(hint, gbc);

        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridy = 2;
        card.add(new JLabel("Username:"), gbc);
        gbc.gridy = 3;
        usernameField = new JTextField(20);
        usernameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Choose username");
        card.add(usernameField, gbc);

        gbc.gridy = 4;
        card.add(new JLabel("Password:"), gbc);
        gbc.gridy = 5;
        passwordField = new JPasswordField(20);
        passwordField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Choose password");
        card.add(passwordField, gbc);

        gbc.gridy = 6;
        card.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridy = 7;
        confirmField = new JPasswordField(20);
        confirmField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Confirm password");
        card.add(confirmField, gbc);

        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(24, 0, 10, 0);
        JButton registerButton = new JButton("Register");
        registerButton.setPreferredSize(new Dimension(150, 42));
        registerButton.setForeground(new Color(0x1565C0));
        card.add(registerButton, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(24, 0, 10, 0);
        JButton backButton = new JButton("Back to Login");
        backButton.setPreferredSize(new Dimension(150, 42));
        card.add(backButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 0, 0, 0);
        JLabel resultLabel = new JLabel("");
        resultLabel.setFont(resultLabel.getFont().deriveFont(11f));
        card.add(resultLabel, gbc);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);

        registerButton.addActionListener((ActionEvent e) -> register(resultLabel));
        backButton.addActionListener((ActionEvent e) -> {
            dispose();
            LoginScreen loginScreen = new LoginScreen();
            loginScreen.setVisible(true);
        });

        usernameField.requestFocusInWindow();

        pack();
        setMinimumSize(new Dimension(getWidth(), getHeight()));
        setLocationRelativeTo(null);
    }

    private void register(JLabel resultLabel) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            resultLabel.setText("All fields are required.");
            resultLabel.setForeground(new Color(0xC62828));
            return;
        }

        if (!password.equals(confirm)) {
            resultLabel.setText("Passwords do not match.");
            resultLabel.setForeground(new Color(0xC62828));
            return;
        }

        if (DBHelper.registerUser(username, password)) {
            resultLabel.setText("Account created. You can now log in.");
            resultLabel.setForeground(new Color(0x2E7D32));
            usernameField.setText("");
            passwordField.setText("");
            confirmField.setText("");
        } else {
            resultLabel.setText("Username already taken.");
            resultLabel.setForeground(new Color(0xC62828));
        }
    }
}
