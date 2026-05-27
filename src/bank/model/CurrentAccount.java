package bank.model;

import bank.exception.InsufficientFundsException;
import bank.exception.InvalidAmountException;
import bank.util.BankUtil;

public class CurrentAccount extends BankAccount implements AccountServices {
    private static final long serialVersionUID = 1L;
    private static final double ANNUAL_INTEREST_RATE = 0.0;
    private final double overdraftLimit;

    public CurrentAccount(String accountHolderName, double initialBalance, double overdraftLimit) {
        super(BankUtil.generateAccountNumber(), accountHolderName, initialBalance, "Current");
        this.overdraftLimit = overdraftLimit;
    }

    public double getOverdraftLimit() { return overdraftLimit; }

    @Override
    public void withdraw(double amount) throws InsufficientFundsException, InvalidAmountException {
        if (amount <= 0) throw new InvalidAmountException("Withdrawal amount must be positive.");
        if (balance + overdraftLimit < amount)
            throw new InsufficientFundsException(balance + overdraftLimit, amount);
        balance -= amount;
        recordTransaction(Transaction.Type.WITHDRAWAL, amount, "Withdrawal");
    }

    @Override
    public double checkBalance() { return balance; }

    @Override
    public void fundTransfer(BankAccount toAccount, double amount)
            throws InsufficientFundsException, InvalidAmountException {
        if (amount <= 0) throw new InvalidAmountException("Transfer amount must be positive.");
        if (balance + overdraftLimit < amount)
            throw new InsufficientFundsException(balance + overdraftLimit, amount);
        balance -= amount;
        recordTransaction(Transaction.Type.TRANSFER_OUT, amount, "Transfer to " + toAccount.getAccountNumber());
        toAccount.balance += amount;
        toAccount.recordTransaction(Transaction.Type.TRANSFER_IN, amount, "Transfer from " + accountNumber);
    }

    @Override
    public void applyInterest() { /* No interest on current accounts */ }

    @Override
    public double getAnnualInterestRate() { return ANNUAL_INTEREST_RATE; }
}
