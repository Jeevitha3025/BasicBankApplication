import bank.server.BankHttpServer;
import bank.service.BankService;

/**
 * ABC Bank Management System v2.0
 *
 * Run modes:
 *   java Main          → starts web server at http://localhost:8080
 *   java Main --cli    → launches interactive terminal UI
 *
 * Core Java concepts: OOP, interfaces, abstract classes, generics,
 * custom exceptions, serialization, streams, HTTP server, REST API
 */
public class Main {
    public static void main(String[] args) throws Exception {
        BankService service = new BankService();

        if (args.length > 0 && "--cli".equals(args[0])) {
            new bank.ui.BankCLI(service).start();
        } else {
            BankHttpServer server = new BankHttpServer(service);
            server.start();
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
            Thread.currentThread().join(); // keep alive
        }
    }
}
