# ABC Bank Management System

A fully-featured **Core Java** banking application demonstrating enterprise-grade design patterns, OOP principles, and clean architecture — built without any external frameworks.

---

## Features

| Feature | Details |
|---|---|
| Account Types | Savings (₹1000 min, 4% p.a.) & Current (overdraft facility) |
| Transactions | Deposit, Withdrawal, Fund Transfer |
| Transaction History | Full per-account log with timestamps & IDs |
| Interest Engine | Monthly interest calculation for Savings accounts |
| Data Persistence | Accounts survive restarts via binary serialization |
| Exception Handling | Custom checked exception hierarchy |
| Architecture | Clean separation: Model → Service → UI |

---

## Project Structure

```
src/
├── Main.java                          # Entry point
└── bank/
    ├── model/
    │   ├── BankAccount.java           # Abstract base class
    │   ├── SavingsAccount.java        # Savings with min-balance & interest
    │   ├── CurrentAccount.java        # Current with overdraft
    │   ├── Transaction.java           # Immutable transaction record
    │   └── AccountServices.java       # Interface: transfer, interest, balance
    ├── service/
    │   └── BankService.java           # Business logic layer
    ├── exception/
    │   ├── AccountNotFoundException.java
    │   ├── InsufficientFundsException.java
    │   └── InvalidAmountException.java
    ├── util/
    │   ├── BankUtil.java              # Account number generator, formatters
    │   └── DataPersistence.java       # Save/load accounts to disk
    └── ui/
        └── BankCLI.java               # Console UI
```

---

## Core Java Concepts Demonstrated

- **OOP**: Inheritance, Polymorphism, Abstraction, Encapsulation
- **Interfaces**: `AccountServices` with default contract
- **Abstract Classes**: `BankAccount` as base with abstract `withdraw()`
- **Custom Exceptions**: Checked exceptions with meaningful messages
- **Collections**: `List`, `ArrayList`, `Stream API`, `Optional`
- **Generics**: Type-safe collections
- **Serialization**: `Serializable` for data persistence
- **Concurrency-safe**: `AtomicInteger` for account number generation
- **Java Records / Pattern Matching**: `instanceof` pattern matching (Java 16+)
- **Immutability**: `Transaction` objects are immutable; `Collections.unmodifiableList()`

---

## How to Run

### Prerequisites
- Java 17+ (uses pattern matching for `instanceof`)

### Compile & Run
```bash
# From project root
javac -d out -sourcepath src src/Main.java src/bank/**/*.java

# Run
java -cp out Main
```

Or with a single command (Java 11+):
```bash
find src -name "*.java" > sources.txt
javac -d out @sources.txt
java -cp out Main
```

---

## Sample Session

```
═══════════════════════════════════════════════════════════════════
  ██████╗  █████╗ ███╗   ██╗██╗  ██╗
  ...
              BANK MANAGEMENT SYSTEM  v2.0
═══════════════════════════════════════════════════════════════════

  [1]  Create Account         [7]  Fund Transfer
  [2]  View Account Details   [8]  Check Balance
  [3]  View All Accounts      [9]  Transaction History
  [4]  Update Account Name    [10] Apply Monthly Interest
  [5]  Deposit                [11] Bank Summary
  [6]  Withdraw               [0]  Exit
```

---

## Exception Hierarchy

```
Exception
└── AccountNotFoundException      → Account with given ID not found
└── InvalidAmountException        → Negative amount, invalid name, etc.
└── InsufficientFundsException    → Balance too low (includes available vs requested)
```

---

## Data Persistence

Account data is saved to `data/accounts.dat` automatically after every transaction. On next startup, all accounts and their full transaction history are restored.

---

## Author

**Jeevitha A** — B.E. CSBS, BMS Institute of Technology and Management, Bengaluru  
GitHub: [github.com/Jeevitha3025](https://github.com/Jeevitha3025)
