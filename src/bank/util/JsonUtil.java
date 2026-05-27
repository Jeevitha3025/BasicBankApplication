package bank.util;

import bank.model.BankAccount;
import bank.model.CurrentAccount;
import bank.model.Transaction;

import java.util.List;

public class JsonUtil {
    private JsonUtil() {}

    public static String toJson(BankAccount acc) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"accountNumber\":\"").append(acc.getAccountNumber()).append("\",");
        sb.append("\"accountHolderName\":\"").append(escape(acc.getAccountHolderName())).append("\",");
        sb.append("\"accountType\":\"").append(acc.getAccountType()).append("\",");
        sb.append("\"balance\":").append(String.format("%.2f", acc.getBalance()));
        if (acc instanceof CurrentAccount ca) {
            sb.append(",\"overdraftLimit\":").append(String.format("%.2f", ca.getOverdraftLimit()));
            sb.append(",\"availableFunds\":").append(String.format("%.2f", ca.getBalance() + ca.getOverdraftLimit()));
        }
        sb.append(",\"transactionCount\":").append(acc.getTransactionHistory().size());
        sb.append("}");
        return sb.toString();
    }

    public static String toJsonWithHistory(BankAccount acc) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"accountNumber\":\"").append(acc.getAccountNumber()).append("\",");
        sb.append("\"accountHolderName\":\"").append(escape(acc.getAccountHolderName())).append("\",");
        sb.append("\"accountType\":\"").append(acc.getAccountType()).append("\",");
        sb.append("\"balance\":").append(String.format("%.2f", acc.getBalance()));
        if (acc instanceof CurrentAccount ca) {
            sb.append(",\"overdraftLimit\":").append(String.format("%.2f", ca.getOverdraftLimit()));
            sb.append(",\"availableFunds\":").append(String.format("%.2f", ca.getBalance() + ca.getOverdraftLimit()));
        }
        sb.append(",\"transactions\":[");
        List<Transaction> txns = acc.getTransactionHistory();
        for (int i = 0; i < txns.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJson(txns.get(i)));
        }
        sb.append("]}");
        return sb.toString();
    }

    public static String toJson(Transaction t) {
        return "{" +
            "\"id\":\"" + t.getTransactionId() + "\"," +
            "\"type\":\"" + t.getType() + "\"," +
            "\"amount\":" + String.format("%.2f", t.getAmount()) + "," +
            "\"balanceAfter\":" + String.format("%.2f", t.getBalanceAfter()) + "," +
            "\"description\":\"" + escape(t.getDescription()) + "\"," +
            "\"timestamp\":\"" + t.getTimestamp().toString() + "\"" +
            "}";
    }

    public static String accountsArray(List<BankAccount> accounts) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < accounts.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJson(accounts.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    public static String success(String message) {
        return "{\"success\":true,\"message\":\"" + escape(message) + "\"}";
    }

    public static String success(String message, String dataKey, String dataValue) {
        return "{\"success\":true,\"message\":\"" + escape(message) + "\",\"" + dataKey + "\":" + dataValue + "}";
    }

    public static String error(String message) {
        return "{\"success\":false,\"error\":\"" + escape(message) + "\"}";
    }

    public static String getField(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) {
            search = "\"" + key + "\":";
            start = json.indexOf(search);
            if (start == -1) return null;
            start += search.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            return json.substring(start, end).trim();
        }
        start += search.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
