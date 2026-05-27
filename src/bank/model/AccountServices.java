package bank.model;

import bank.exception.InsufficientFundsException;
import bank.exception.InvalidAmountException;

public interface AccountServices {
    double checkBalance();
    void fundTransfer(BankAccount toAccount, double amount) throws InsufficientFundsException, InvalidAmountException;
    void applyInterest();
    double getAnnualInterestRate();
}
