package bank.server;

import bank.exception.AccountNotFoundException;
import bank.exception.InsufficientFundsException;
import bank.exception.InvalidAmountException;
import bank.model.BankAccount;
import bank.service.BankService;
import bank.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

/**
 * Lightweight HTTP server exposing ABC Bank REST API.
 * Uses only built-in Java classes — no external dependencies.
 *
 * Endpoints:
 *   GET  /api/accounts               → all accounts
 *   GET  /api/accounts/{id}          → account details
 *   GET  /api/accounts/{id}/history  → transaction history
 *   POST /api/accounts/savings        → create savings account
 *   POST /api/accounts/current        → create current account
 *   PUT  /api/accounts/{id}/name      → update holder name
 *   POST /api/accounts/{id}/deposit   → deposit
 *   POST /api/accounts/{id}/withdraw  → withdraw
 *   POST /api/transfer                → fund transfer
 *   POST /api/accounts/{id}/interest  → apply interest
 *   POST /api/interest/all            → apply interest to all
 *   GET  /api/summary                 → bank summary
 *   GET  /                            → serve index.html
 */
public class BankHttpServer {
    private static final Logger logger = Logger.getLogger(BankHttpServer.class.getName());
    private static final int PORT = 8080;
    private final BankService service;
    private HttpServer server;

    public BankHttpServer(BankService service) {
        this.service = service;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/", this::handleApi);
        server.createContext("/", this::handleStatic);
        server.setExecutor(null);
        server.start();
        logger.info("ABC Bank server running at http://localhost:" + PORT);
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   ABC Bank Server started successfully!  ║");
        System.out.println("║   Open: http://localhost:" + PORT + "            ║");
        System.out.println("║   Press Ctrl+C to stop                   ║");
        System.out.println("╚══════════════════════════════════════════╝");
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    // ─── Static file handler ─────────────────────────────────────────────────

    private void handleStatic(HttpExchange ex) throws IOException {
        String uri = ex.getRequestURI().getPath();
        if (uri.equals("/") || uri.equals("/index.html")) {
            Path htmlPath = Paths.get("web/index.html");
            if (Files.exists(htmlPath)) {
                byte[] bytes = Files.readAllBytes(htmlPath);
                ex.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                sendResponse(ex, 200, bytes);
            } else {
                sendJson(ex, 404, JsonUtil.error("index.html not found in web/"));
            }
        } else {
            sendJson(ex, 404, JsonUtil.error("Not found"));
        }
    }

    // ─── API handler ─────────────────────────────────────────────────────────

    private void handleApi(HttpExchange ex) throws IOException {
        addCorsHeaders(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) { sendResponse(ex, 204, new byte[0]); return; }

        String path   = ex.getRequestURI().getPath();
        String method = ex.getRequestMethod();
        String body   = readBody(ex);

        try {
            String response = route(method, path, body);
            sendJson(ex, 200, response);
        } catch (AccountNotFoundException e) {
            sendJson(ex, 404, JsonUtil.error(e.getMessage()));
        } catch (InsufficientFundsException | InvalidAmountException e) {
            sendJson(ex, 400, JsonUtil.error(e.getMessage()));
        } catch (Exception e) {
            sendJson(ex, 500, JsonUtil.error("Internal error: " + e.getMessage()));
        }
    }

    private String route(String method, String path, String body)
            throws Exception {
        // GET /api/summary
        if ("GET".equals(method) && path.equals("/api/summary")) {
            return buildSummary();
        }
        // GET /api/accounts
        if ("GET".equals(method) && path.equals("/api/accounts")) {
            return JsonUtil.accountsArray(service.getAllAccounts());
        }
        // POST /api/accounts/savings
        if ("POST".equals(method) && path.equals("/api/accounts/savings")) {
            String name    = JsonUtil.getField(body, "name");
            double balance = Double.parseDouble(JsonUtil.getField(body, "balance"));
            BankAccount acc = service.createSavingsAccount(name, balance);
            return JsonUtil.success("Savings account created", "account", JsonUtil.toJson(acc));
        }
        // POST /api/accounts/current
        if ("POST".equals(method) && path.equals("/api/accounts/current")) {
            String name      = JsonUtil.getField(body, "name");
            double balance   = Double.parseDouble(JsonUtil.getField(body, "balance"));
            double overdraft = Double.parseDouble(JsonUtil.getField(body, "overdraftLimit"));
            BankAccount acc  = service.createCurrentAccount(name, balance, overdraft);
            return JsonUtil.success("Current account created", "account", JsonUtil.toJson(acc));
        }
        // POST /api/transfer
        if ("POST".equals(method) && path.equals("/api/transfer")) {
            String from   = JsonUtil.getField(body, "from");
            String to     = JsonUtil.getField(body, "to");
            double amount = Double.parseDouble(JsonUtil.getField(body, "amount"));
            service.transfer(from, to, amount);
            return JsonUtil.success("Transfer of ₹" + String.format("%.2f", amount) + " completed");
        }
        // POST /api/interest/all
        if ("POST".equals(method) && path.equals("/api/interest/all")) {
            service.applyInterestToAll();
            return JsonUtil.success("Monthly interest applied to all eligible accounts");
        }

        // Account-specific paths: /api/accounts/{id}/...
        String[] parts = path.split("/");
        if (parts.length >= 4 && "accounts".equals(parts[2])) {
            String accountId = parts[3];
            String subPath   = parts.length >= 5 ? parts[4] : "";

            if ("GET".equals(method) && subPath.isEmpty()) {
                return JsonUtil.toJson(service.getAccount(accountId));
            }
            if ("GET".equals(method) && "history".equals(subPath)) {
                return JsonUtil.toJsonWithHistory(service.getAccount(accountId));
            }
            if ("PUT".equals(method) && "name".equals(subPath)) {
                String name = JsonUtil.getField(body, "name");
                service.updateAccountName(accountId, name);
                return JsonUtil.success("Name updated successfully");
            }
            if ("POST".equals(method) && "deposit".equals(subPath)) {
                double amount = Double.parseDouble(JsonUtil.getField(body, "amount"));
                service.deposit(accountId, amount);
                BankAccount acc = service.getAccount(accountId);
                return JsonUtil.success("Deposited ₹" + String.format("%.2f", amount),
                        "balance", String.valueOf(acc.getBalance()));
            }
            if ("POST".equals(method) && "withdraw".equals(subPath)) {
                double amount = Double.parseDouble(JsonUtil.getField(body, "amount"));
                service.withdraw(accountId, amount);
                BankAccount acc = service.getAccount(accountId);
                return JsonUtil.success("Withdrawn ₹" + String.format("%.2f", amount),
                        "balance", String.valueOf(acc.getBalance()));
            }
            if ("POST".equals(method) && "interest".equals(subPath)) {
                service.applyMonthlyInterest(accountId);
                BankAccount acc = service.getAccount(accountId);
                return JsonUtil.success("Interest applied",
                        "balance", String.valueOf(acc.getBalance()));
            }
        }

        return JsonUtil.error("Route not found: " + method + " " + path);
    }

    private String buildSummary() {
        List<BankAccount> all = service.getAllAccounts();
        long savings = all.stream().filter(a -> "Savings".equals(a.getAccountType())).count();
        long current = all.stream().filter(a -> "Current".equals(a.getAccountType())).count();
        double total = service.getTotalDeposits();
        double avg   = all.isEmpty() ? 0 : total / all.size();
        return String.format(
            "{\"totalAccounts\":%d,\"savingsAccounts\":%d,\"currentAccounts\":%d," +
            "\"totalFunds\":%.2f,\"averageBalance\":%.2f}",
            all.size(), savings, current, total, avg);
    }

    // ─── HTTP helpers ─────────────────────────────────────────────────────────

    private void addCorsHeaders(HttpExchange ex) {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private void sendJson(HttpExchange ex, int status, String json) throws IOException {
        sendResponse(ex, status, json.getBytes(StandardCharsets.UTF_8));
    }

    private void sendResponse(HttpExchange ex, int status, byte[] body) throws IOException {
        if (!ex.getResponseHeaders().containsKey("Content-Type"))
            ex.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(status, body.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(body); }
    }

    private String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
