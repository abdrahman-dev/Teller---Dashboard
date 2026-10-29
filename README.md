# Teller — Account Management System

A simple Java Swing desktop application for managing bank-style customer accounts. Built for a university Java Programming (2) course project.

## Features

- **Login Screen** — Admin authentication with username/password stored in SQLite
- **View Accounts** — All accounts displayed in a table (ID, owner name, account number, balance)
- **Add Account** — Create a new account with owner name and initial balance
- **Delete Account** — Remove an existing account (with confirmation dialog)
- **Deposit** — Add funds to an account
- **Withdraw** — Subtract funds (rejected if amount exceeds balance)
- **Auto Refresh** — Table updates after every operation
- **Seeded demo data** — ~8 realistic accounts pre-loaded on first run

## Tech Stack

- Java 21, Swing GUI
- SQLite (local file database, no server setup)
- FlatLaf (modern flat look and feel)
- JDBC for database connectivity

## Project Structure

```
AccountManagementSystem/
├── src/
│   ├── Main.java                 (Application entry point)
│   ├── DBHelper.java             (Database connection + all SQL queries + seed data)
│   ├── LoginScreen.java          (Login GUI)
│   ├── MainScreen.java           (Main dashboard: table + buttons)
│   └── TransactionDialog.java    (Popup for deposit/withdraw)
├── lib/
│   ├── sqlite-jdbc-3.53.4.0.jar  (SQLite JDBC driver)
│   └── flatlaf-3.7.2.jar         (FlatLaf look and feel)
├── setup.sh                      (Mac/Linux/WSL/Git Bash setup)
├── setup.bat                     (Windows cmd setup)
├── .gitignore
├── README.md
└── Account_Management_System.md  (full project report)
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

The script downloads missing jars, compiles the project, and prints the exact run command.

### Option 2: Manual

```bash
javac -cp "lib/sqlite-jdbc-3.53.4.0.jar;lib/flatlaf-3.7.2.jar" -d out src\*.java
java -cp "out;lib/sqlite-jdbc-3.53.4.0.jar;lib/flatlaf-3.7.2.jar" Main
```

*(Use `:` instead of `;` on Mac/Linux)*

## Default Login

- **Username:** `admin`
- **Password:** `admin123`

## Notes

- The SQLite database file (`accounts.db`) is created automatically on first run — no server setup needed.
- Demo accounts are seeded automatically if the accounts table is empty.
- The `out/` directory and `accounts.db` are gitignored.
