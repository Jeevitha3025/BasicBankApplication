package bank.model;

import bank.exception.InsufficientFundsException;
import bank.exception.InvalidAmountException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BankAccount implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final String accountNumber;
    protected String accountHolderName;
    protected double balance;
    protected final String accountType;
    protected final List<Transaction> transactionHistory;
    private int txnCounter = 1;

    public BankAccount(String accountNumber, String accountHolderName, double initialBalance, String accountType) {
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.balance = initialBalance;
        this.accountType = accountType;
        this.transactionHistory = new ArrayList<>();
        recordTransaction(Transaction.Type.DEPOSIT, initialBalance, "Account opened");
    }

    public void deposit(double amount) throws InvalidAmountException {
        if (amount <= 0) throw new InvalidAmountException("Deposit amount must be positive.");
        balance += amount;
        recordTransaction(Transaction.Type.DEPOSIT, amount, "Deposit");
    }

    public abstract void withdraw(double amount) throws InsufficientFundsException, InvalidAmountException;

    protected void recordTransaction(Transaction.Type type, double amount, String description) {
        String txnId = accountNumber + "-T" + String.format("%04d", txnCounter++);
        transactionHistory.add(new Transaction(txnId, type, amount, balance, description));
    }

    public List<Transaction> getTransactionHistory() {
        return Collections.unmodifiableList(transactionHistory);
    }

    public String getAccountNumber()    { return accountNumber; }
    public String getAccountHolderName() { return accountHolderName; }
    public String getAccountType()      { return accountType; }
    public double getBalance()          { return balance; }

    public void setAccountHolderName(String name) {
        if (name == null || name.trim().isEmpty()) return;
        this.accountHolderName = name.trim();
    }

    public String getSummary() {
        return String.format("%-10s | %-20s | %-10s | ₹%12.2f",
                accountNumber, accountHolderName, accountType, balance);
    }
}
