package bank.exception;

public class InsufficientFundsException extends Exception {
    private final double availableBalance;
    private final double requestedAmount;

    public InsufficientFundsException(double available, double requested) {
        super(String.format("Insufficient funds. Available: %.2f, Requested: %.2f", available, requested));
        this.availableBalance = available;
        this.requestedAmount = requested;
    }

    public double getAvailableBalance() { return availableBalance; }
    public double getRequestedAmount() { return requestedAmount; }
}
