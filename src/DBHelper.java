import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DBHelper {

    private static final String DB_URL = "jdbc:sqlite:accounts.db";
    private static final SimpleDateFormat TS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(64) NOT NULL, " +
                    "role VARCHAR(20) DEFAULT 'ADMIN')");
            stmt.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "owner_name VARCHAR(100) NOT NULL, " +
                    "account_number VARCHAR(20) NOT NULL, " +
                    "balance DOUBLE DEFAULT 0, " +
                    "account_type VARCHAR(20) DEFAULT 'SAVINGS')");
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "account_id INTEGER NOT NULL, " +
                    "type VARCHAR(20) NOT NULL, " +
                    "amount DOUBLE NOT NULL, " +
                    "timestamp TEXT NOT NULL)");

            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM users");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO users (username, password, role) VALUES (?, ?, ?)")) {
                        ins.setString(1, "admin");
                        ins.setString(2, hashPassword("admin123"));
                        ins.setString(3, "ADMIN");
                        ins.executeUpdate();
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM accounts");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    seedDemoData(conn);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void seedDemoData(Connection conn) throws SQLException {
        String[][] demo = {
            {"Sarah Chen", "ACC-10024", "5240.00", "SAVINGS"},
            {"Marcus Williams", "ACC-10031", "12500.75", "CHECKING"},
            {"Priya Patel", "ACC-10047", "890.50", "SAVINGS"},
            {"Diego Rodriguez", "ACC-10053", "0.00", "SAVINGS"},
            {"Elena Kowalski", "ACC-10062", "34200.10", "SAVINGS"},
            {"Jamal Okafor", "ACC-10078", "150.00", "CHECKING"},
            {"Lena Schmidt", "ACC-10089", "7650.00", "SAVINGS"},
            {"Thomas Nguyen", "ACC-10095", "420.25", "CHECKING"}
        };

        List<Integer> accountIds = new ArrayList<>();
        try (PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO accounts (owner_name, account_number, balance, account_type) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            for (String[] row : demo) {
                ins.setString(1, row[0]);
                ins.setString(2, row[1]);
                ins.setDouble(3, Double.parseDouble(row[2]));
                ins.setString(4, row[3]);
                ins.executeUpdate();
                try (ResultSet keys = ins.getGeneratedKeys()) {
                    if (keys.next()) {
                        accountIds.add(keys.getInt(1));
                    }
                }
            }
        }

        String now = TS.format(new Date());
        try (PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO transactions (account_id, type, amount, timestamp) VALUES (?, ?, ?, ?)")) {
            for (int i = 0; i < accountIds.size(); i++) {
                int aid = accountIds.get(i);
                double bal = Double.parseDouble(demo[i][2]);
                ins.setInt(1, aid);
                ins.setString(2, "Deposit");
                ins.setDouble(3, bal);
                ins.setString(4, now);
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
            ps.setString(2, hashPassword(password));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean registerUser(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }
        if (usernameExists(username)) {
            return false;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users (username, password, role) VALUES (?, ?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, hashPassword(password));
            ps.setString(3, "ADMIN");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean usernameExists(String username) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id FROM users WHERE username = ?")) {
            ps.setString(1, username);
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
                     "SELECT id, owner_name, account_number, balance, account_type FROM accounts ORDER BY id")) {
            while (rs.next()) {
                accounts.add(new Object[] {
                        rs.getInt("id"),
                        rs.getString("owner_name"),
                        rs.getString("account_number"),
                        rs.getDouble("balance"),
                        rs.getString("account_type")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
    }

    public static boolean addAccount(String ownerName, String accountNumber, double balance, String accountType) {
        if (ownerName == null || ownerName.isEmpty() || accountNumber == null || accountNumber.isEmpty()) {
            return false;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO accounts (owner_name, account_number, balance, account_type) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, ownerName);
            ps.setString(2, accountNumber);
            ps.setDouble(3, balance);
            ps.setString(4, accountType);
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
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance + ? WHERE id = ?")) {
                ps.setDouble(1, amount);
                ps.setInt(2, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO transactions (account_id, type, amount, timestamp) VALUES (?, 'Deposit', ?, ?)")) {
                ps.setInt(1, id);
                ps.setDouble(2, amount);
                ps.setString(3, TS.format(new Date()));
                ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean withdraw(int id, double amount) {
        if (amount <= 0) {
            return false;
        }
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance - ? WHERE id = ? AND balance >= ?")) {
                ps.setDouble(1, amount);
                ps.setInt(2, id);
                ps.setDouble(3, amount);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    conn.rollback();
                    return false;
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO transactions (account_id, type, amount, timestamp) VALUES (?, 'Withdraw', ?, ?)")) {
                ps.setInt(1, id);
                ps.setDouble(2, amount);
                ps.setString(3, TS.format(new Date()));
                ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Object[] getStats() {
        int totalAccounts = 0;
        double totalBalance = 0;
        int below50 = 0;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*), COALESCE(SUM(balance), 0), SUM(CASE WHEN balance < 50 THEN 1 ELSE 0 END) FROM accounts")) {
            if (rs.next()) {
                totalAccounts = rs.getInt(1);
                totalBalance = rs.getDouble(2);
                below50 = rs.getInt(3);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Object[]{totalAccounts, totalBalance, below50};
    }

    public static List<Object[]> getTransactions(int accountId) {
        List<Object[]> transactions = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT type, amount, timestamp FROM transactions WHERE account_id = ? ORDER BY id DESC")) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new Object[] {
                            rs.getString("type"),
                            rs.getDouble("amount"),
                            rs.getString("timestamp")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public static boolean applyInterest() {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, balance FROM accounts WHERE account_type = 'SAVINGS'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        double balance = rs.getDouble("balance");
                        double interest = balance * 0.02;
                        double newBalance = balance + interest;

                        try (PreparedStatement up = conn.prepareStatement(
                                "UPDATE accounts SET balance = ? WHERE id = ?")) {
                            up.setDouble(1, newBalance);
                            up.setInt(2, id);
                            up.executeUpdate();
                        }

                        try (PreparedStatement ins = conn.prepareStatement(
                                "INSERT INTO transactions (account_id, type, amount, timestamp) VALUES (?, 'Interest', ?, ?)")) {
                            ins.setInt(1, id);
                            ins.setDouble(2, interest);
                            ins.setString(3, TS.format(new Date()));
                            ins.executeUpdate();
                        }
                    }
                }
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean undoLastTransaction(int accountId) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, type, amount FROM transactions WHERE account_id = ? ORDER BY id DESC LIMIT 1")) {
                ps.setInt(1, accountId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    int txId = rs.getInt("id");
                    String type = rs.getString("type");
                    double amount = rs.getDouble("amount");

                    double adjustment;
                    String undoType;
                    if ("Deposit".equals(type)) {
                        adjustment = -amount;
                        undoType = "Undo Deposit";
                    } else if ("Withdraw".equals(type)) {
                        adjustment = amount;
                        undoType = "Undo Withdraw";
                    } else {
                        conn.rollback();
                        return false;
                    }

                    try (PreparedStatement up = conn.prepareStatement(
                            "UPDATE accounts SET balance = balance + ? WHERE id = ?")) {
                        up.setDouble(1, adjustment);
                        up.setInt(2, accountId);
                        up.executeUpdate();
                    }

                    try (PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO transactions (account_id, type, amount, timestamp) VALUES (?, ?, ?, ?)")) {
                        ins.setInt(1, accountId);
                        ins.setString(2, undoType);
                        ins.setDouble(3, Math.abs(adjustment));
                        ins.setString(4, TS.format(new Date()));
                        ins.executeUpdate();
                    }

                    conn.commit();
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
