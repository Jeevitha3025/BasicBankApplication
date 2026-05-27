package bank.ui;

import bank.exception.AccountNotFoundException;
import bank.exception.InsufficientFundsException;
import bank.exception.InvalidAmountException;
import bank.model.BankAccount;
import bank.model.CurrentAccount;
import bank.model.Transaction;
import bank.service.BankService;

import java.util.List;
import java.util.Scanner;

/**
 * Console-based user interface for the ABC Bank application.
 */
public class BankCLI {
    private final BankService service;
    private final Scanner scanner;

    private static final String DIVIDER = "═".repeat(65);
    private static final String LINE    = "─".repeat(65);

    public BankCLI(BankService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        printBanner();
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter choice: ");
            System.out.println();
            switch (choice) {
                case 1  -> createAccount();
                case 2  -> viewAccount();
                case 3  -> viewAllAccounts();
                case 4  -> updateName();
                case 5  -> deposit();
                case 6  -> withdraw();
                case 7  -> transfer();
                case 8  -> checkBalance();
                case 9  -> viewTransactionHistory();
                case 10 -> applyInterest();
                case 11 -> bankSummary();
                case 0  -> { running = false; System.out.println("Thank you for banking with ABC Bank. Goodbye!"); }
                default -> System.out.println("  ✗ Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    // ─── Menu Actions ────────────────────────────────────────────────────────

    private void createAccount() {
        System.out.println(DIVIDER);
        System.out.println("  CREATE NEW ACCOUNT");
        System.out.println(DIVIDER);
        System.out.println("  [1] Savings Account  (Min balance: ₹1000, Interest: 4% p.a.)");
        System.out.println("  [2] Current Account  (Overdraft facility, No interest)");
        int type = readInt("  Select type: ");
        String name = readString("  Account Holder Name: ");

        try {
            if (type == 1) {
                double balance = readDouble("  Initial Balance (min ₹1000): ");
                var acc = service.createSavingsAccount(name, balance);
                System.out.println("\n  ✓ Savings Account created successfully!");
                System.out.println("  Account Number : " + acc.getAccountNumber());
                System.out.println("  Account Holder : " + acc.getAccountHolderName());
                System.out.println("  Opening Balance: ₹" + acc.getBalance());
            } else if (type == 2) {
                double balance  = readDouble("  Initial Balance: ");
                double overdraft = readDouble("  Overdraft Limit: ");
                var acc = service.createCurrentAccount(name, balance, overdraft);
                System.out.println("\n  ✓ Current Account created successfully!");
                System.out.println("  Account Number  : " + acc.getAccountNumber());
                System.out.println("  Account Holder  : " + acc.getAccountHolderName());
                System.out.println("  Opening Balance : ₹" + acc.getBalance());
                System.out.println("  Overdraft Limit : ₹" + ((CurrentAccount) acc).getOverdraftLimit());
            } else {
                System.out.println("  ✗ Invalid type selected.");
            }
        } catch (InvalidAmountException e) {
            System.out.println("  ✗ " + e.getMessage());
        }
    }

    private void viewAccount() {
        System.out.println(DIVIDER);
        System.out.println("  ACCOUNT DETAILS");
        System.out.println(DIVIDER);
        String accNum = readString("  Account Number: ");
        try {
            BankAccount acc = service.getAccount(accNum);
            System.out.println();
            System.out.println("  Account Number  : " + acc.getAccountNumber());
            System.out.println("  Account Holder  : " + acc.getAccountHolderName());
            System.out.println("  Account Type    : " + acc.getAccountType());
            System.out.println("  Current Balance : ₹" + String.format("%.2f", acc.getBalance()));
            if (acc instanceof CurrentAccount ca) {
                System.out.println("  Overdraft Limit : ₹" + String.format("%.2f", ca.getOverdraftLimit()));
                System.out.println("  Available Funds : ₹" + String.format("%.2f", ca.getBalance() + ca.getOverdraftLimit()));
            }
            System.out.println("  Total Txns      : " + acc.getTransactionHistory().size());
        } catch (AccountNotFoundException e) {
            System.out.println("  ✗ " + e.getMessage());
        }
    }

    private void viewAllAccounts() {
        System.out.println(DIVIDER);
        System.out.println("  ALL ACCOUNTS");
        System.out.println(DIVIDER);
        List<BankAccount> all = service.getAllAccounts();
        if (all.isEmpty()) {
            System.out.println("  No accounts found.");
            return;
        }
        System.out.printf("  %-10s | %-20s | %-10s | %14s%n", "ACC NO.", "HOLDER", "TYPE", "BALANCE");
        System.out.println(LINE);
        for (BankAccount acc : all) {
            System.out.println("  " + acc.getSummary());
        }
        System.out.println(LINE);
        System.out.printf("  %-10s   %-20s   %-10s   ₹%12.2f%n",
                "", "TOTAL FUNDS", "", service.getTotalDeposits());
    }

    private void updateName() {
        System.out.println(DIVIDER);
        System.out.println("  UPDATE ACCOUNT HOLDER NAME");
        System.out.println(DIVIDER);
        String accNum  = readString("  Account Number: ");
        String newName = readString("  New Name      : ");
        try {
            service.updateAccountName(accNum, newName);
            System.out.println("  ✓ Name updated successfully.");
        } catch (AccountNotFoundException | InvalidAmountException e) {
            System.out.println("  ✗ " + e.getMessage());
        }
    }

    private void deposit() {
        System.out.println(DIVIDER);
        System.out.println("  DEPOSIT");
        System.out.println(DIVIDER);
        String accNum = readString("  Account Number: ");
        double amount = readDouble("  Amount        : ₹");
        try {
            service.deposit(accNum, amount);
            double newBalance = service.getAccount(accNum).getBalance();
            System.out.println("  ✓ Deposited ₹" + String.format("%.2f", amount));
            System.out.println("  New Balance : ₹" + String.format("%.2f", newBalance));
        } catch (AccountNotFoundException | InvalidAmountException e) {
            System.out.println("  ✗ " + e.getMessage());
        }
    }

    private void withdraw() {
        System.out.println(DIVIDER);
        System.out.println("  WITHDRAWAL");
        System.out.println(DIVIDER);
        String accNum = readString("  Account Number: ");
        double amount = readDouble("  Amount        : ₹");
        try {
            service.withdraw(accNum, amount);
            double newBalance = service.getAccount(accNum).getBalance();
            System.out.println("  ✓ Withdrawn ₹" + String.format("%.2f", amount));
            System.out.println("  New Balance : ₹" + String.format("%.2f", newBalance));
        } catch (AccountNotFoundException | InsufficientFundsException | InvalidAmountException e) {
            System.out.println("  ✗ " + e.getMessage());
        }
    }

    private void transfer() {
        System.out.println(DIVIDER);
        System.out.println("  FUND TRANSFER");
        System.out.println(DIVIDER);
        String from   = readString("  From Account: ");
        String to     = readString("  To Account  : ");
        double amount = readDouble("  Amount      : ₹");
        try {
            service.transfer(from, to, amount);
            System.out.println("  ✓ Transferred ₹" + String.format("%.2f", amount));
            System.out.println("  From Balance: ₹" + String.format("%.2f", service.getAccount(from).getBalance()));
            System.out.println("  To Balance  : ₹" + String.format("%.2f", service.getAccount(to).getBalance()));
        } catch (AccountNotFoundException | InsufficientFundsException | InvalidAmountException e) {
            System.out.println("  ✗ " + e.getMessage());
        }
    }

    private void checkBalance() {
        System.out.println(DIVIDER);
        System.out.println("  BALANCE INQUIRY");
        System.out.println(DIVIDER);
        String accNum = readString("  Account Number: ");
        try {
            BankAccount acc = service.getAccount(accNum);
            System.out.println("  Account : " + acc.getAccountNumber() + " (" + acc.getAccountType() + ")");
            System.out.println("  Balance : ₹" + String.format("%.2f", acc.getBalance()));
        } catch (AccountNotFoundException e) {
            System.out.println("  ✗ " + e.getMessage());
        }
    }

    private void viewTransactionHistory() {
        System.out.println(DIVIDER);
        System.out.println("  TRANSACTION HISTORY");
        System.out.println(DIVIDER);
        String accNum = readString("  Account Number: ");
        try {
            BankAccount acc = service.getAccount(accNum);
            List<Transaction> history = acc.getTransactionHistory();
            if (history.isEmpty()) {
                System.out.println("  No transactions found.");
                return;
            }
            System.out.println("\n  Account: " + accNum + " | " + acc.getAccountHolderName());
            System.out.println(LINE);
            for (Transaction t : history) {
                System.out.println("  " + t);
            }
            System.out.println(LINE);
            System.out.println("  Total transactions: " + history.size());
        } catch (AccountNotFoundException e) {
            System.out.println("  ✗ " + e.getMessage());
        }
    }

    private void applyInterest() {
        System.out.println(DIVIDER);
        System.out.println("  APPLY MONTHLY INTEREST");
        System.out.println(DIVIDER);
        System.out.println("  [1] Apply to a specific account");
        System.out.println("  [2] Apply to all eligible accounts");
        int choice = readInt("  Choice: ");
        if (choice == 1) {
            String accNum = readString("  Account Number: ");
            try {
                service.applyMonthlyInterest(accNum);
                double newBalance = service.getAccount(accNum).getBalance();
                System.out.println("  ✓ Interest applied. New Balance: ₹" + String.format("%.2f", newBalance));
            } catch (AccountNotFoundException e) {
                System.out.println("  ✗ " + e.getMessage());
            }
        } else if (choice == 2) {
            service.applyInterestToAll();
            System.out.println("  ✓ Monthly interest applied to all eligible accounts.");
        }
    }

    private void bankSummary() {
        System.out.println(DIVIDER);
        System.out.println("  BANK SUMMARY");
        System.out.println(DIVIDER);
        List<BankAccount> all = service.getAllAccounts();
        long savings  = all.stream().filter(a -> "Savings".equals(a.getAccountType())).count();
        long current  = all.stream().filter(a -> "Current".equals(a.getAccountType())).count();
        double total  = service.getTotalDeposits();
        System.out.println("  Total Accounts    : " + all.size());
        System.out.println("  Savings Accounts  : " + savings);
        System.out.println("  Current Accounts  : " + current);
        System.out.printf ("  Total Funds Held  : ₹%.2f%n", total);
        System.out.printf ("  Avg Account Value : ₹%.2f%n", all.isEmpty() ? 0 : total / all.size());
    }

    // ─── Print Helpers ───────────────────────────────────────────────────────

    private void printBanner() {
        System.out.println(DIVIDER);
        System.out.println("  ██████╗  █████╗ ███╗   ██╗██╗  ██╗");
        System.out.println("  ██╔══██╗██╔══██╗████╗  ██║██║ ██╔╝");
        System.out.println("  ██████╔╝███████║██╔██╗ ██║█████╔╝ ");
        System.out.println("  ██╔══██╗██╔══██║██║╚██╗██║██╔═██╗ ");
        System.out.println("  ██████╔╝██║  ██║██║ ╚████║██║  ██╗");
        System.out.println("  ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═══╝╚═╝  ╚═╝");
        System.out.println("              BANK MANAGEMENT SYSTEM  v2.0");
        System.out.println(DIVIDER);
        System.out.printf ("  Accounts loaded: %d%n", service.getAllAccounts().size());
        System.out.println(DIVIDER);
    }

    private void printMenu() {
        System.out.println("\n" + DIVIDER);
        System.out.println("  MAIN MENU");
        System.out.println(DIVIDER);
        System.out.println("  [1]  Create Account         [7]  Fund Transfer");
        System.out.println("  [2]  View Account Details   [8]  Check Balance");
        System.out.println("  [3]  View All Accounts      [9]  Transaction History");
        System.out.println("  [4]  Update Account Name    [10] Apply Monthly Interest");
        System.out.println("  [5]  Deposit                [11] Bank Summary");
        System.out.println("  [6]  Withdraw               [0]  Exit");
        System.out.println(DIVIDER);
    }

    // ─── Input Helpers ───────────────────────────────────────────────────────

    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int val = Integer.parseInt(scanner.nextLine().trim());
                return val;
            } catch (NumberFormatException e) {
                System.out.println("  ✗ Please enter a valid number.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double val = Double.parseDouble(scanner.nextLine().trim());
                return val;
            } catch (NumberFormatException e) {
                System.out.println("  ✗ Please enter a valid amount.");
            }
        }
    }
}
