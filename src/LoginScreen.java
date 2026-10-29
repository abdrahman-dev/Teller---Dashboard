import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.AbstractAction;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class LoginScreen extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginScreen() {
        setTitle("Teller — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(new EmptyBorder(30, 50, 30, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel title = new JLabel("Teller");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setForeground(new Color(0x1565C0));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(title, gbc);

        JLabel hint = new JLabel("Sign in to continue");
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
        usernameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter username");
        card.add(usernameField, gbc);

        gbc.gridy = 4;
        card.add(new JLabel("Password:"), gbc);
        gbc.gridy = 5;
        passwordField = new JPasswordField(20);
        passwordField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter password");
        card.add(passwordField, gbc);

        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(24, 0, 0, 0);
        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(150, 42));
        loginButton.setForeground(new Color(0x1565C0));
        card.add(loginButton, gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(16, 0, 0, 0);
        JLabel credentials = new JLabel("Default: admin / admin123");
        credentials.setFont(credentials.getFont().deriveFont(11f));
        card.add(credentials, gbc);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);

        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        card.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enter, "submitLogin");
        card.getActionMap().put("submitLogin", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        getRootPane().setDefaultButton(loginButton);

        loginButton.addActionListener((ActionEvent e) -> login());

        usernameField.requestFocusInWindow();

        pack();
        setMinimumSize(new Dimension(getWidth(), getHeight()));
        setLocationRelativeTo(null);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Username and password cannot be empty.",
                    "Login Error", JOptionPane.ERROR_MESSAGE);
            usernameField.requestFocusInWindow();
            return;
        }

        if (DBHelper.authenticate(username, password)) {
            dispose();
            MainScreen mainScreen = new MainScreen();
            mainScreen.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password.",
                    "Login Error", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            usernameField.requestFocusInWindow();
        }
    }
}
