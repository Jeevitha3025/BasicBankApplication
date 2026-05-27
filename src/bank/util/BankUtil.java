package bank.util;

import java.util.concurrent.atomic.AtomicInteger;

public class BankUtil {
    private static final AtomicInteger counter = new AtomicInteger(1000);
    private static final String BANK_PREFIX = "ABC";

    private BankUtil() {}

    public static String generateAccountNumber() {
        return BANK_PREFIX + counter.getAndIncrement();
    }

    public static void setCounterStart(int value) {
        counter.set(value);
    }

    public static int getCounterValue() {
        return counter.get();
    }

    public static String formatCurrency(double amount) {
        return String.format("₹%.2f", amount);
    }

    public static boolean isValidAmount(double amount) {
        return amount > 0 && !Double.isNaN(amount) && !Double.isInfinite(amount);
    }
}
