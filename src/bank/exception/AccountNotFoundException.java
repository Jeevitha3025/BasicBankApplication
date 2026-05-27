package bank.exception;

public class AccountNotFoundException extends Exception {
    public AccountNotFoundException(String accountNumber) {
        super("Account not found: " + accountNumber);
    }
}
