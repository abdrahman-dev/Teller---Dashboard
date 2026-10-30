# Teller — Account Management System

A Java Swing desktop application for managing bank-style customer accounts. Built for a university Java Programming (2) course project. The project uses seven plain Java files, an SQLite database, and the FlatLaf light theme.

## Features

- **Login** — Admin authentication against the `users` table. Passwords are stored as SHA-256 hashes, not plain text.
- **Create Admin Account** — Register a new admin from the login screen. Checks that fields are filled, the passwords match, and the username is not already taken.
- **View Accounts** — Table with ID, owner name, account number, balance, and account type.
- **Search** — Live filter by owner name or account number (case-insensitive).
- **Sort** — Click a column header to sort the accounts table.
- **Add Account** — Owner name, opening balance, and account type (SAVINGS or CHECKING). The account number is generated automatically.
- **Delete Account** — Remove an account after a confirmation dialog.
- **Deposit** — Add funds to an account. Every deposit is written to the `transactions` table.
- **Withdraw** — Subtract funds. Rejected if the amount is not positive or is greater than the current balance.
- **Transaction History** — Separate window showing one account's transactions, newest first, with type, amount, and date/time.
- **Undo Last Transaction** — Reverses the most recent deposit or withdrawal for the selected account and records an undo entry in the history.
- **Apply Interest** — Adds 2% interest to every SAVINGS account in one step, after a confirmation dialog. Each credit is logged as a transaction.
- **Export CSV** — Saves the accounts currently shown in the table (respecting the active search filter) to a CSV file.
- **Summary Cards** — Total accounts, total balance, and the number of accounts below $50. Updated after every operation.
- **Balance Colors** — Green for a normal balance, amber below $50, red at zero or below.
- **Seeded Data** — On first run the database gets a default admin and eight demo accounts with an opening transaction each.

## Tech Stack

- Java 21, Swing GUI
- SQLite (local file database, no server setup)
- FlatLaf 3.7.2 (flat look and feel)
- JDBC via sqlite-jdbc 3.53.4.0

## Project Structure

```
JavaUniProject/
├── src/
│   ├── Main.java                     (Entry point: FlatLaf setup, database init, login screen)
│   ├── DBHelper.java                 (Connection, schema, seed data, hashing, all SQL queries)
│   ├── LoginScreen.java              (Login form and link to registration)
│   ├── RegisterScreen.java           (Create a new admin account)
│   ├── MainScreen.java               (Dashboard: table, search, stats, actions, CSV export)
│   ├── TransactionDialog.java        (Deposit/withdraw dialog)
│   └── TransactionHistoryDialog.java (Transaction history window)
├── lib/
│   ├── sqlite-jdbc-3.53.4.0.jar      (SQLite JDBC driver)
│   └── flatlaf-3.7.2.jar             (FlatLaf look and feel)
├── setup.sh                          (Mac/Linux/WSL/Git Bash setup)
├── setup.bat                         (Windows cmd setup)
├── .gitignore
└── README.md
```

## How to Run

### Option 1: Setup script (recommended)

**Mac/Linux/WSL/Git Bash:**
```bash
./setup.sh
```

**Windows (cmd):**
```cmd
setup.bat
```

The script creates `lib/` if needed, downloads any missing jars from Maven Central, compiles all files in `src/` to `out/`, and prints the run command.

### Option 2: Manual

Windows (cmd):
```cmd
javac -cp "lib\sqlite-jdbc-3.53.4.0.jar;lib\flatlaf-3.7.2.jar" -d out src\*.java
java -cp "out;lib\sqlite-jdbc-3.53.4.0.jar;lib\flatlaf-3.7.2.jar" Main
```

Mac/Linux:
```bash
javac -cp "lib/sqlite-jdbc-3.53.4.0.jar:lib/flatlaf-3.7.2.jar" -d out src/*.java
java -cp "out:lib/sqlite-jdbc-3.53.4.0.jar:lib/flatlaf-3.7.2.jar" Main
```

In VS Code, open the project folder and use the Run button from the Java extension. If the jars are not recognized, add the two files in `lib/` under Java Projects > Referenced Libraries.

## Default Login

- **Username:** `admin`
- **Password:** `admin123`

The password is stored as a SHA-256 hash in the database. The value above is what you type at login.

## Database

The SQLite file `accounts.db` is created automatically in the working directory on first run. It contains three tables.

**users**

| Column | Type | Notes |
| --- | --- | --- |
| id | INTEGER | Primary key, auto increment |
| username | VARCHAR(50) | Unique, not null |
| password | VARCHAR(64) | SHA-256 hash, not null |
| role | VARCHAR(20) | Defaults to ADMIN |

**accounts**

| Column | Type | Notes |
| --- | --- | --- |
| id | INTEGER | Primary key, auto increment |
| owner_name | VARCHAR(100) | Not null |
| account_number | VARCHAR(20) | Not null |
| balance | DOUBLE | Defaults to 0 |
| account_type | VARCHAR(20) | SAVINGS or CHECKING, defaults to SAVINGS |

**transactions**

| Column | Type | Notes |
| --- | --- | --- |
| id | INTEGER | Primary key, auto increment |
| account_id | INTEGER | Account the transaction belongs to, not null |
| type | VARCHAR(20) | Deposit, Withdraw, Interest, Undo Deposit, or Undo Withdraw |
| amount | DOUBLE | Not null |
| timestamp | TEXT | Date and time as yyyy-MM-dd HH:mm:ss |

`transactions.account_id` refers to `accounts.id`, so one account can have many transactions. The link is handled in the application code; no FOREIGN KEY constraint is declared. The `users` table only holds admin logins, and accounts are not tied to a user row.

## Notes

- The database file is created on first run. No server or manual setup is needed.
- The default admin and the eight demo accounts are inserted only when the corresponding tables are empty.
- The `out/` directory and `accounts.db` are gitignored, along with IDE and OS files.
