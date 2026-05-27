package bank.util;

import bank.model.BankAccount;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataPersistence {
    private static final Logger logger = Logger.getLogger(DataPersistence.class.getName());
    private static final String DATA_FILE = "data/accounts.dat";
    private static final String COUNTER_FILE = "data/counter.dat";

    private DataPersistence() {}

    public static void saveAccounts(List<BankAccount> accounts) {
        new File("data").mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(new ArrayList<>(accounts));
            saveCounter();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not save accounts: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static List<BankAccount> loadAccounts() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<BankAccount> accounts = (List<BankAccount>) ois.readObject();
            loadCounter();
            return accounts;
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.WARNING, "Could not load accounts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static void saveCounter() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(COUNTER_FILE))) {
            dos.writeInt(BankUtil.getCounterValue());
        }
    }

    private static void loadCounter() {
        File file = new File(COUNTER_FILE);
        if (!file.exists()) return;
        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            BankUtil.setCounterStart(dis.readInt());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not load counter: " + e.getMessage());
        }
    }
}
