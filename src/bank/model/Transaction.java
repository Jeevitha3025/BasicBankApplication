package bank.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public enum Type { DEPOSIT, WITHDRAWAL, TRANSFER_OUT, TRANSFER_IN, INTEREST }

    private final String transactionId;
    private final Type type;
    private final double amount;
    private final double balanceAfter;
    private final LocalDateTime timestamp;
    private final String description;

    public Transaction(String transactionId, Type type, double amount, double balanceAfter, String description) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = LocalDateTime.now();
        this.description = description;
    }

    public String getTransactionId() { return transactionId; }
    public Type getType()            { return type; }
    public double getAmount()        { return amount; }
    public double getBalanceAfter()  { return balanceAfter; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDescription()   { return description; }

    @Override
    public String toString() {
        return String.format("[%s] %-15s | ₹%10.2f | Balance: ₹%10.2f | %s | %s",
                transactionId, type, amount, balanceAfter,
                timestamp.format(FORMATTER), description);
    }
}
