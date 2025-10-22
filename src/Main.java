import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class Transaction {
    private static int nextId = 1;
    private int id;
    private String type;
    private double amount;
    private LocalDateTime timestamp;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Transaction(String type, double amount) {
        this.id = nextId++;
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }

    public int getId() { return id; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getTime() { return timestamp.format(formatter); }
}

class BankAccount {
    private String accNumber;
    private String username;
    private String password;
    private double balance;
    private List<Transaction> trans;
    private boolean isActive;

    public BankAccount(String accNumber, String username, String password) {
        this.accNumber = accNumber;
        this.username = username;
        this.password = password;
        this.balance = 0;
        this.trans = new ArrayList<>();
        this.isActive = true;
    }

    public boolean checkPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    public void deposit(double amount) {
        if (!isActive) throw new RuntimeException("Account is not active");
        if (amount <= 0) throw new RuntimeException("Amount must be positive");

        balance += amount;
        trans.add(new Transaction("DEPOSIT", amount));
        System.out.printf("Deposited: $%.2f\n", amount);
    }

    public void withdraw(double amount) {
        if (!isActive) throw new RuntimeException("Account is not active");
        if (amount <= 0) throw new RuntimeException("Amount must be positive");
        if (amount > balance) throw new RuntimeException("Not enough money");

        balance -= amount;
        trans.add(new Transaction("WITHDRAWAL", amount));
        System.out.printf("Withdrawn: $%.2f\n", amount);
    }

    public void showBalance() {
        if (!isActive) throw new RuntimeException("Account is not active");
        System.out.printf("\n balance: $%.2f\n", balance);
    }

    public void transHist() {
        if (!isActive) throw new RuntimeException("Account is not active");

        if (trans.isEmpty()) {
            System.out.print("No transactions\n");
            return;
        }

        System.out.print("\nTransaction History:\n");
        for (Transaction t : trans) {
            System.out.print(t.getId() + " | " + t.getTime() + " | " + t.getType() + " | $" + t.getAmount());
            System.out.print("\n");
        }
    }

    public void searchTrans(String type, Double minAmount, Double maxAmount) {
        if (!isActive) throw new RuntimeException("Account not active");

        List<Transaction> results = new ArrayList<>();
        for (Transaction t : trans) {
            if (type != null && !t.getType().equals(type)) continue;
            if (minAmount != null && t.getAmount() < minAmount) continue;
            if (maxAmount != null && t.getAmount() > maxAmount) continue;
            results.add(t);
        }

        if (results.isEmpty()) {
            System.out.print("No matching transactions\n");
        }
        else {
            System.out.print("\nFound " + results.size() + " transactions:");
            System.out.print("\n");
            for (Transaction t : results) {
                System.out.print(t.getId() + " | " + t.getTime() + " | " + t.getType() + " | $" + t.getAmount());
                System.out.print("\n");
            }
        }
    }

    public String getAccNum() { return accNumber; }
    public String getUsername() { return username; }
    public double getBalance() { return balance; }
}

class BankingSystem {
    private static BankAccount curAcc = null;
    private static Scanner scanner = new Scanner(System.in);
    private static Map<String, BankAccount> accounts = new HashMap<>();

    public static void main(String[] args) {
        System.out.print("BANK SYSTEM\n");

        while (true) {
            try {
                mainMenu();
                System.out.print("Select option: ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.print("Please enter a number\n");
                    continue;
                }

                int choice = Integer.parseInt(input);

                switch (choice) {
                    case 1:
                        createNewAcc();
                        break;
                    case 2:
                        logToAcc();
                        break;
                    case 3:
                        allAcc();
                        break;
                    case 0:
                        System.out.print("Goodbye!\n");
                        return;
                    default:
                        System.out.print("Invalid option\n");
                }
            } catch (Exception e) {
                System.out.print("Error: " + e.getMessage()+"\n");
            }
            System.out.print("\n");
        }
    }

    private static void mainMenu() {
        System.out.print("1. Create account\n");
        System.out.print("2. Login\n");
        System.out.print("3. View all accounts\n");
        System.out.print("0. Exit\n");
    }

    private static void accMenu() {
        System.out.print("\nACCOUNT MENU\n");
        System.out.print("User: " + curAcc.getUsername()+"\n");
        System.out.print("Account: " + curAcc.getAccNum()+"\n");
        System.out.print("1. Deposit\n");
        System.out.print("2. Withdraw\n");
        System.out.print("3. Check balance\n");
        System.out.print("4. Transaction history\n");
        System.out.print("5. Search transactions\n");
        System.out.print("6. Logout\n");
    }
    private static double isPos() {
        while (true){
            try {
                String input = scanner.nextLine().trim();
                double value = Double.parseDouble(input.replace(',', '.'));
                if (value <= 0) {
                    System.out.print("\nAmount must be positive");
                    continue;
                }
                return value;
            }
            catch (NumberFormatException e) {
                System.out.print("\nInvalid number, enter again");
            }
        }
    }

    private static Double isPosOrNull() {
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return null;
        try {
            return Double.parseDouble(input.replace(',', '.'));
        }
        catch (NumberFormatException e) {
            System.out.print("\nInvalid number");
            return null;
        }
    }
    private static void createNewAcc() {
        System.out.print("\nCREATE NEW ACCOUNT\n");

        String username;
        while (true) {
            System.out.print("Enter your name: ");
            username = scanner.nextLine().trim();
            if (!username.isEmpty()) break;
            System.out.print("Name can not be empty\n");
        }

        String password;
        while (true) {
            System.out.print("Enter your password: ");
            password = scanner.nextLine().trim();
            if (!password.isEmpty()) break;
            System.out.print("Password can not be empty\n");
        }

        System.out.print("Confirm password: ");
        String password2 = scanner.nextLine().trim();
        if (!password2.equals(password)) {
            System.out.print("Passwords are different\n");
            return;
        }

        String accNum = numberGenerator();
        curAcc = new BankAccount(accNum, username, password);
        accounts.put(accNum, curAcc);

        System.out.print("\nAccount created successfully!\n");
        System.out.print("Account number: " + accNum+"\n");
        System.out.print("Owner: " + username+"\n");
        accSession();
    }

    private static String numberGenerator() {
        Random random = new Random();
        String number = "";
        number += (1 + random.nextInt(9));
        for (int i = 0; i < 9; i++) {
            number += random.nextInt(10);
        }
        return number;
    }

    private static void logToAcc() {
        System.out.print("\nLogin");

        if (accounts.isEmpty()) {
            System.out.print("No accounts exist yet\n");
            return;
        }

        System.out.print("Account number: ");
        String accNum = scanner.nextLine().trim();

        System.out.print("\nPassword: ");
        String password = scanner.nextLine().trim();

        BankAccount account = accounts.get(accNum);
        if (account == null) {
            System.out.print("\nAccount not found");
            return;
        }

        if (!account.checkPassword(password)) {
            System.out.print("\nWrong password");
            return;
        }

        curAcc = account;
        System.out.print("\nWelcome back, " + curAcc.getUsername() + "!\n");
        accSession();
    }

    private static void allAcc() {
        System.out.print("\nAll accounts:\n");
        if (accounts.isEmpty()) {
            System.out.print("\nNo accounts\n");
            return;
        }

        for (BankAccount acc : accounts.values()) {
            System.out.print(acc.getAccNum() + " | " + acc.getUsername() + " | $" + acc.getBalance()+"\n");
        }
    }

    private static void accSession() {
        while (true) {
            try {
                accMenu();
                System.out.print("\nChoose action: ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.print("Please select an option\n");
                    continue;
                }

                int choice = Integer.parseInt(input);

                switch (choice) {
                    case 1:
                        System.out.print("\nEnter deposit amount: $");
                        double dep = isPos();
                        curAcc.deposit(dep);
                        break;
                    case 2:
                        System.out.print("\nEnter withdrawal amount: $");
                        double wthd = isPos();
                        curAcc.withdraw(wthd);
                        break;
                    case 3:
                        curAcc.showBalance();
                        break;
                    case 4:
                        curAcc.transHist();
                        break;
                    case 5:
                        search();
                        break;
                    case 6:
                        System.out.print("See you soon," + curAcc.getUsername() +"!\n");
                        curAcc = null;
                        return;
                    default:
                        System.out.print("Invalid choice\n");
                }
            } catch (Exception e) {
                System.out.print("Error: " + e.getMessage()+"\n");
            }
        }
    }

    private static void search() {
        System.out.print("\nSearch transactions:\nType (1-deposit, 2-withdrawal, enter-skip):");
        String typeInput = scanner.nextLine().trim();
        String type = null;
        if (typeInput.equals("1")) type = "DEPOSIT";
        else if (typeInput.equals("2")) type = "WITHDRAWAL";
        System.out.print("\nMin amount $: ");
        Double minAmount = isPosOrNull();
        System.out.print("\nMax amount $: ");
        Double maxAmount = isPosOrNull();

        curAcc.searchTrans(type, minAmount, maxAmount);
    }
}