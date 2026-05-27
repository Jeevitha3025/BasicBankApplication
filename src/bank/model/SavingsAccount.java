package bank.model;

import bank.exception.InsufficientFundsException;
import bank.exception.InvalidAmountException;
import bank.util.BankUtil;

public class SavingsAccount extends BankAccount implements AccountServices {
    private static final long serialVersionUID = 1L;
    public static final double MIN_BALANCE = 1000.0;
    private static final double ANNUAL_INTEREST_RATE = 4.0; // 4% p.a.

    public SavingsAccount(String accountHolderName, double initialBalance) {
        super(BankUtil.generateAccountNumber(), accountHolderName, initialBalance, "Savings");
    }

    @Override
    public void withdraw(double amount) throws InsufficientFundsException, InvalidAmountException {
        if (amount <= 0) throw new InvalidAmountException("Withdrawal amount must be positive.");
        if (balance - amount < MIN_BALANCE)
            throw new InsufficientFundsException(balance - MIN_BALANCE, amount);
        balance -= amount;
        recordTransaction(Transaction.Type.WITHDRAWAL, amount, "Withdrawal");
    }

    @Override
    public double checkBalance() { return balance; }

    @Override
    public void fundTransfer(BankAccount toAccount, double amount)
            throws InsufficientFundsException, InvalidAmountException {
        if (amount <= 0) throw new InvalidAmountException("Transfer amount must be positive.");
        if (balance - amount < MIN_BALANCE)
            throw new InsufficientFundsException(balance - MIN_BALANCE, amount);
        balance -= amount;
        recordTransaction(Transaction.Type.TRANSFER_OUT, amount, "Transfer to " + toAccount.getAccountNumber());
        toAccount.balance += amount;
        toAccount.recordTransaction(Transaction.Type.TRANSFER_IN, amount, "Transfer from " + accountNumber);
    }

    @Override
    public void applyInterest() {
        double interest = balance * (ANNUAL_INTEREST_RATE / 100.0) / 12;
        balance += interest;
        recordTransaction(Transaction.Type.INTEREST, interest,
                String.format("Monthly interest @ %.1f%% p.a.", ANNUAL_INTEREST_RATE));
    }

    @Override
    public double getAnnualInterestRate() { return ANNUAL_INTEREST_RATE; }
}
