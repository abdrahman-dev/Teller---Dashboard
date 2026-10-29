# Teller
### Account Management System

**Faculty of Industrial and Energy Technology**
**Information Technology Department — 3rd Year**
**Course:** Java Programming (2)
**Term:** 1st Term 2026/2027
**Lecturer:** Dr. Mohammed AbdelFattah

---

## 1. Project Overview

**Teller** is a desktop application built with Java (Swing) and a SQLite database for managing customer accounts in a simple banking-style system. It allows an administrator to log in, manage accounts, and perform deposits and withdrawals.

This project fulfills the course requirement of building a system with both a graphical user interface (GUI) and a corresponding database.

---

## 2. Objectives

- Apply Object-Oriented Programming concepts using Java.
- Build a functional desktop GUI using Java Swing.
- Connect a Java application to a SQLite database using JDBC.
- Implement basic CRUD operations (Create, Read, Update, Delete) on account data.
- Apply simple validation logic (e.g., preventing withdrawals that exceed the balance).

---

## 3. Tools & Technologies

| Component | Technology |
|---|---|
| Programming Language | Java |
| GUI Framework | Java Swing |
| Look and Feel | FlatLaf (`FlatLightLaf`) |
| Database | SQLite (local file: `accounts.db`) |
| Database Connector | JDBC (`org.xerial:sqlite-jdbc`) |
| IDE | NetBeans / IntelliJ IDEA / Eclipse |

---

## 4. System Features

1. **Login Screen** — Admin logs in using a username and password stored in the database.
2. **View Accounts** — All accounts are displayed in a table showing account number, owner name, and balance.
3. **Add Account** — Create a new account with an owner name and an initial balance.
4. **Delete Account** — Remove an existing account (with confirmation dialog).
5. **Deposit** — Add a specified amount to an account's balance.
6. **Withdraw** — Subtract a specified amount from an account's balance, with validation to prevent the balance from going negative.
7. **Auto Refresh** — The accounts table updates automatically after every operation.
8. **Seeded Demo Data** — ~8 realistic accounts are pre-loaded on first run.

---

## 5. Database Design

The system uses two tables in a local SQLite file (`accounts.db`), created automatically on first run — no server setup required.

### `users` table

Stores login credentials for the system administrator.

| Column | Type | Description |
|---|---|---|
| id | INTEGER (PK, AUTOINCREMENT) | Unique user ID |
| username | VARCHAR(50) | Login username |
| password | VARCHAR(50) | Login password |
| role | VARCHAR(20) | User role (default: ADMIN) |

### `accounts` table

Stores customer account records.

| Column | Type | Description |
|---|---|---|
| id | INTEGER (PK, AUTOINCREMENT) | Unique account ID |
| owner_name | VARCHAR(100) | Name of the account holder |
| account_number | VARCHAR(20) | Account number |
| balance | DOUBLE | Current account balance |

### Default Admin Login

- **Username:** `admin`
- **Password:** `admin123`

---

## 6. System Architecture

The project follows a simple, lightweight structure with clear separation between database logic and the GUI.

```
AccountManagementSystem/
├── src/
│   ├── Main.java                 (Application entry point)
│   ├── DBHelper.java             (Database connections & queries + seed data)
│   ├── LoginScreen.java          (Login GUI)
│   ├── MainScreen.java           (Main dashboard: table + buttons)
│   └── TransactionDialog.java    (Popup window for deposit/withdraw)
├── lib/
│   ├── sqlite-jdbc-3.53.4.0.jar  (SQLite JDBC driver)
│   └── flatlaf-3.7.2.jar         (FlatLaf look and feel)
├── setup.sh                      (Mac/Linux/WSL/Git Bash setup script)
├── setup.bat                     (Windows cmd setup script)
├── .gitignore
└── README.md
```

### Class Responsibilities

| Class | Responsibility |
|---|---|
| `Main.java` | Launches the application, sets FlatLaf light theme, opens the Login screen |
| `DBHelper.java` | SQLite connection and all SQL queries (login check, fetch/add/delete accounts, update balance, seed data) |
| `LoginScreen.java` | GUI for entering username/password and verifying login |
| `MainScreen.java` | Displays the accounts table and buttons (Add, Delete, Deposit, Withdraw, Refresh, Logout) |
| `TransactionDialog.java` | Popup window for entering a deposit/withdraw amount |

---

## 7. Workflow

1. The user launches the application → Login Screen appears.
2. The user enters a username and password → validated against the `users` table.
3. On successful login → Main Screen opens, displaying all accounts from the database.
4. The user can add, delete, deposit to, or withdraw from accounts. The table refreshes automatically after each operation.
5. Clicking **Logout** returns to the Login screen.

---

## 8. Validation Rules

- Username and password fields cannot be empty at login.
- Deposit/withdraw amount must be a valid positive number.
- Withdrawal is rejected if the amount exceeds the current account balance.
- Owner name and initial balance are required when adding a new account.
- Initial balance cannot be negative.

---

## 9. Getting Started

**Mac/Linux/WSL/Git Bash:**
```bash
./setup.sh
```

**Windows (cmd):**
```cmd
setup.bat
```

Each script downloads missing jars, compiles the project, and prints the run command.

### Manual build

```bash
javac -cp "lib/sqlite-jdbc-3.53.4.0.jar;lib/flatlaf-3.7.2.jar" -d out src\*.java
java -cp "out;lib/sqlite-jdbc-3.53.4.0.jar;lib/flatlaf-3.7.2.jar" Main
```

*(Use `:` instead of `;` on Mac/Linux)*

---

## 10. Team Members & Task Distribution

| # | Name | Responsibility |
|---|---|---|
| 1 | *(Name)* | Database design + `DBHelper.java` |
| 2 | *(Name)* | `LoginScreen.java` |
| 3 | *(Name)* | `MainScreen.java` + `TransactionDialog.java` |

*(Fill in team member names before submission.)*

---

## 11. Conclusion

Teller meets the assignment requirements with a Java Swing GUI and SQLite database backend, demonstrating GUI event handling, JDBC connectivity, and basic data validation.

---

## 12. References

- Oracle Java Documentation: https://docs.oracle.com/javase/
- SQLite JDBC Driver (xerial): https://github.com/xerial/sqlite-jdbc
- FlatLaf Look and Feel: https://www.formdev.com/flatlaf/
- Java Swing Tutorial: https://docs.oracle.com/javase/tutorial/uiswing/
