import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBHelper {

    private static final String DB_URL = "jdbc:sqlite:accounts.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(50) NOT NULL, " +
                    "role VARCHAR(20) DEFAULT 'ADMIN')");
            stmt.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "owner_name VARCHAR(100) NOT NULL, " +
                    "account_number VARCHAR(20) NOT NULL, " +
                    "balance DOUBLE DEFAULT 0)");

            // Seed default admin user if users table is empty
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM users");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO users (username, password, role) VALUES (?, ?, ?)")) {
                        ins.setString(1, "admin");
                        ins.setString(2, "admin123");
                        ins.setString(3, "ADMIN");
                        ins.executeUpdate();
                    }
                }
            }

            // Seed demo accounts if accounts table is empty (only on first run)
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM accounts");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    seedDemoAccounts(conn);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void seedDemoAccounts(Connection conn) throws SQLException {
        String[][] demo = {
            {"Sarah Chen", "ACC-10024", "5240.00"},
            {"Marcus Williams", "ACC-10031", "12500.75"},
            {"Priya Patel", "ACC-10047", "890.50"},
            {"Diego Rodriguez", "ACC-10053", "0.00"},
            {"Elena Kowalski", "ACC-10062", "34200.10"},
            {"Jamal Okafor", "ACC-10078", "150.00"},
            {"Lena Schmidt", "ACC-10089", "7650.00"},
            {"Thomas Nguyen", "ACC-10095", "420.25"}
        };
        try (PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO accounts (owner_name, account_number, balance) VALUES (?, ?, ?)")) {
            for (String[] row : demo) {
                ins.setString(1, row[0]);
                ins.setString(2, row[1]);
                ins.setDouble(3, Double.parseDouble(row[2]));
                ins.addBatch();
            }
            ins.executeBatch();
        }
    }

    public static boolean authenticate(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id FROM users WHERE username = ? AND password = ?")) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Object[]> getAllAccounts() {
        List<Object[]> accounts = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT id, owner_name, account_number, balance FROM accounts ORDER BY id")) {
            while (rs.next()) {
                accounts.add(new Object[] {
                        rs.getInt("id"),
                        rs.getString("owner_name"),
                        rs.getString("account_number"),
                        rs.getDouble("balance")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
    }

    public static boolean addAccount(String ownerName, String accountNumber, double balance) {
        if (ownerName == null || ownerName.isEmpty() || accountNumber == null || accountNumber.isEmpty()) {
            return false;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO accounts (owner_name, account_number, balance) VALUES (?, ?, ?)")) {
            ps.setString(1, ownerName);
            ps.setString(2, accountNumber);
            ps.setDouble(3, balance);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteAccount(int id) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM accounts WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deposit(int id, double amount) {
        if (amount <= 0) {
            return false;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE accounts SET balance = balance + ? WHERE id = ?")) {
            ps.setDouble(1, amount);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean withdraw(int id, double amount) {
        if (amount <= 0) {
            return false;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE accounts SET balance = balance - ? WHERE id = ? AND balance >= ?")) {
            ps.setDouble(1, amount);
            ps.setInt(2, id);
            ps.setDouble(3, amount);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
