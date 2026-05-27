package bank.service;

import bank.exception.AccountNotFoundException;
import bank.exception.InsufficientFundsException;
import bank.exception.InvalidAmountException;
import bank.model.*;
import bank.util.DataPersistence;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service layer encapsulating all bank business logic.
 * Handles account creation, transactions, and data persistence.
 */
public class BankService {
    private final List<BankAccount> accounts;

    public BankService() {
        this.accounts = DataPersistence.loadAccounts();
    }

    // ─── Account Management ──────────────────────────────────────────────────

    public SavingsAccount createSavingsAccount(String holderName, double initialBalance)
            throws InvalidAmountException {
        validateName(holderName);
        if (initialBalance < SavingsAccount.MIN_BALANCE)
            throw new InvalidAmountException("Minimum opening balance for Savings is ₹" + SavingsAccount.MIN_BALANCE);
        SavingsAccount account = new SavingsAccount(holderName.trim(), initialBalance);
        accounts.add(account);
        save();
        return account;
    }

    public CurrentAccount createCurrentAccount(String holderName, double initialBalance, double overdraftLimit)
            throws InvalidAmountException {
        validateName(holderName);
        if (initialBalance < 0) throw new InvalidAmountException("Initial balance cannot be negative.");
        if (overdraftLimit < 0) throw new InvalidAmountException("Overdraft limit cannot be negative.");
        CurrentAccount account = new CurrentAccount(holderName.trim(), initialBalance, overdraftLimit);
        accounts.add(account);
        save();
        return account;
    }

    public void updateAccountName(String accountNumber, String newName)
            throws AccountNotFoundException, InvalidAmountException {
        validateName(newName);
        findAccountOrThrow(accountNumber).setAccountHolderName(newName.trim());
        save();
    }

    // ─── Transactions ────────────────────────────────────────────────────────

    public void deposit(String accountNumber, double amount)
            throws AccountNotFoundException, InvalidAmountException {
        findAccountOrThrow(accountNumber).deposit(amount);
        save();
    }

    public void withdraw(String accountNumber, double amount)
            throws AccountNotFoundException, InsufficientFundsException, InvalidAmountException {
        findAccountOrThrow(accountNumber).withdraw(amount);
        save();
    }

    public void transfer(String fromAccountNumber, String toAccountNumber, double amount)
            throws AccountNotFoundException, InsufficientFundsException, InvalidAmountException {
        if (fromAccountNumber.equals(toAccountNumber))
            throw new InvalidAmountException("Cannot transfer to the same account.");
        BankAccount from = findAccountOrThrow(fromAccountNumber);
        BankAccount to   = findAccountOrThrow(toAccountNumber);
        if (!(from instanceof AccountServices))
            throw new InvalidAmountException("This account type does not support transfers.");
        ((AccountServices) from).fundTransfer(to, amount);
        save();
    }

    public void applyMonthlyInterest(String accountNumber)
            throws AccountNotFoundException {
        BankAccount account = findAccountOrThrow(accountNumber);
        if (account instanceof AccountServices) {
            ((AccountServices) account).applyInterest();
            save();
        }
    }

    public void applyInterestToAll() {
        accounts.stream()
                .filter(a -> a instanceof AccountServices)
                .forEach(a -> ((AccountServices) a).applyInterest());
        save();
    }

    // ─── Queries ─────────────────────────────────────────────────────────────

    public BankAccount getAccount(String accountNumber) throws AccountNotFoundException {
        return findAccountOrThrow(accountNumber);
    }

    public List<BankAccount> getAllAccounts() {
        return Collections.unmodifiableList(accounts);
    }

    public double getTotalDeposits() {
        return accounts.stream().mapToDouble(BankAccount::getBalance).sum();
    }

    // ─── Private Helpers ─────────────────────────────────────────────────────

    private BankAccount findAccountOrThrow(String accountNumber) throws AccountNotFoundException {
        Optional<BankAccount> result = accounts.stream()
                .filter(a -> a.getAccountNumber().equalsIgnoreCase(accountNumber))
                .findFirst();
        return result.orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    private void validateName(String name) throws InvalidAmountException {
        if (name == null || name.trim().isEmpty())
            throw new InvalidAmountException("Account holder name cannot be empty.");
    }

    private void save() {
        DataPersistence.saveAccounts(accounts);
    }
}
