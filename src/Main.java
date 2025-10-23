import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//Транзакция(положить на счет/снять со счета) срдержит информацию о ее типе, сумме, времени и id
class Transaction {
    private static int nextId = 1;//общий счетчик транзакций
    private int id;
    private String type;
    private double amount;
    private LocalDateTime timestamp;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");//для вывода времени в красивом формате

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

//Банковский счет, содержит информацию о владельце, балансе и истории операций
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
    //корректность введенного пароля
    public boolean checkPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }
    //положить деньги на счет
    public void deposit(double amount) {
        if (!isActive) throw new RuntimeException("Account is not active");
        if (amount <= 0) throw new RuntimeException("Amount must be positive");

        balance += amount;
        trans.add(new Transaction("DEPOSIT", amount));
        System.out.printf("Deposited: $%.2f\n", amount);
    }
    //снять деньги со счета
    public void withdraw(double amount) {
        if (!isActive) throw new RuntimeException("Account is not active");
        if (amount <= 0) throw new RuntimeException("Amount must be positive");
        if (amount > balance) throw new RuntimeException("Not enough money");

        balance -= amount;
        trans.add(new Transaction("WITHDRAWAL", amount));
        System.out.printf("Withdrawn: $%.2f\n", amount);
    }
    //смотреть баланс
    public void showBalance() {
        if (!isActive) throw new RuntimeException("Account is not active");
        System.out.printf("\n balance: $%.2f\n", balance);
    }
    //история операций
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
    //поиск по транзакциям
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
    //геттеры для привятных полей: номер счета, имя владельца и баланс
    public String getAccNum() { return accNumber; }
    public String getUsername() { return username; }
    public double getBalance() { return balance; }
}
//Банковская система, открывается в начале
class BankingSystem {
    private static BankAccount curAcc = null;
    private static Scanner scanner = new Scanner(System.in);//сканирует ввод пользователя
    private static Map<String, BankAccount> accounts = new HashMap<>();//хранит все счета: номер счета-объект счета

    public static void main(String[] args) {
        System.out.print("BANK SYSTEM\n");

        while (true) {
            try {
                mainMenu();
                System.out.print("Select option: ");
                String input = scanner.nextLine().trim();//читаем ввод и убираем лишние пробелы

                if (input.isEmpty()) {
                    System.out.print("Please enter a number\n");
                    continue;
                }

                int choice = Integer.parseInt(input);
                //выбираем в меню доступные опции
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
                        System.out.print("Goodbye!\n");//при выходе
                        return;
                    default:
                        System.out.print("Invalid option\n");//если неверный ввод
                }
            } catch (Exception e) {
                System.out.print("Error: " + e.getMessage()+"\n");
            }
            System.out.print("\n");
        }
    }
    //опции
    private static void mainMenu() {
        System.out.print("1. Create account\n");
        System.out.print("2. Login\n");
        System.out.print("3. View all accounts\n");
        System.out.print("0. Exit\n");
    }
    //меню при входе в аккаунт
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
    //определяет, является ли сумма положительной
    private static double isPos() {
        while (true){
            try {
                String input = scanner.nextLine().trim();
                double value = Double.parseDouble(input.replace(',', '.'));//читаем сумму и с запятой, и с точкой
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
    //определяет, является ли число положительным, либо пустой ввод
    private static Double isPosOrNull() {
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return null;
        try {
            return Double.parseDouble(input.replace(',', '.'));//читаем сумму и с запятой, и с точкой
        }
        catch (NumberFormatException e) {
            System.out.print("\nInvalid number");
            return null;
        }
    }
    //проверка что имя пользователя состоит из символов
    private static boolean isValidUsername(String username) {
        if (username.isEmpty()) {
            return false;
        }

        for (int i = 0; i < username.length(); i++) {
            char c = username.charAt(i);//выделяем каждый символ
            if (!Character.isLetter(c)) {//смотрим, является ли он бувой
                return false;
            }
        }
        return true;
    }
    //проверка валидности пароля: состоит из 4 цифр
    private static boolean isValidPassword(String password) {
        if (password.length() != 4) {
            return false;
        }
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);//выделяем посимвольно
            if (!Character.isDigit(c)) {//если нашли не цифру
                return false;
            }
        }
        return true;
    }
    //создать новый аккаунт
    private static void createNewAcc() {
        System.out.print("\nCREATE NEW ACCOUNT\n");
        //читаем имя пользователя, пока не будет ввод только из символов
        String username;
        while (true) {
            System.out.print("Enter your name (letters only): ");
            username = scanner.nextLine().trim();

            if (username.isEmpty()) {
                System.out.print("Username can not be empty\n");
                continue;
            }
            if (!isValidUsername(username)) {
                System.out.print("Name must contain only letters\n");
                continue;
            }

            break;
        }
        //просим ввести пароль, пока не будет из 4 цифр
        String password;
        while (true) {
            System.out.print("Enter your password (4 digits): ");
            password = scanner.nextLine().trim();

            if (password.isEmpty()) {
                System.out.print("Password can not be empty\n");
                continue;
            }

            if (!isValidPassword(password)) {
                System.out.print("Password must be 4 digits\n");
                continue;
            }

            break;
        }
        //подтверждение пароля, проверка на их мэтч
        while (true) {
            System.out.print("Confirm password: ");
            String password2 = scanner.nextLine().trim();
            if (!password2.equals(password)) {
                System.out.print("Passwords are different\n");
                continue;
            }
            break;
        }
        String accNum = numberGenerator();//генерируем счет
        curAcc = new BankAccount(accNum, username, password);//создаем новый объект
        accounts.put(accNum, curAcc);//сохраняем немер счета и объект

        System.out.print("\nAccount created successfully!\n");
        System.out.print("Account number: " + accNum+"\n");
        System.out.print("Owner: " + username+"\n");
        accSession();
    }
    //для генерации десятизначного номера
    private static String numberGenerator() {
        Random random = new Random();
        String number = "";
        number += (1 + random.nextInt(9));
        for (int i = 0; i < 9; i++) {
            number += random.nextInt(10);
        }
        return number;
    }
    //вход в аккаунт
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
        //поиск аккаунта по номеру
        BankAccount account = accounts.get(accNum);
        if (account == null) {
            System.out.print("\nAccount not found");
            return;
        }
        //проверка пароля
        if (!account.checkPassword(password)) {
            System.out.print("\nWrong password");
            return;
        }

        curAcc = account;
        System.out.print("\nWelcome back, " + curAcc.getUsername() + "!\n");
        accSession();
    }
    //выводит список всех аккаунтов
    private static void allAcc() {
        System.out.print("\nAll accounts:\n");
        if (accounts.isEmpty()) {
            System.out.print("\nNo accounts\n");
            return;
        }
        //выводит информацию о счете в виде: номер | имя | баланс
        for (BankAccount acc : accounts.values()) {
            System.out.print(acc.getAccNum() + " | " + acc.getUsername() + " | $" + acc.getBalance()+"\n");
        }
    }
    //работа после авторизации
    private static void accSession() {
        while (true) {
            try {
                accMenu();
                System.out.print("\nChoose action: ");
                String input = scanner.nextLine().trim();
                //если пустой ввод
                if (input.isEmpty()) {
                    System.out.print("Please select an option\n");
                    continue;
                }

                int choice = Integer.parseInt(input);
                //выбор операции
                switch (choice) {
                    case 1://пополнение
                        System.out.print("\nEnter deposit amount: $");
                        double dep = isPos();
                        curAcc.deposit(dep);
                        break;
                    case 2://снятие
                        System.out.print("\nEnter withdrawal amount: $");
                        double wthd = isPos();
                        curAcc.withdraw(wthd);
                        break;
                    case 3://баланс
                        curAcc.showBalance();
                        break;
                    case 4://история операций
                        curAcc.transHist();
                        break;
                    case 5://поиск по транзакциям
                        search();
                        break;
                    case 6://выход из аккаунта
                        System.out.print("See you soon," + curAcc.getUsername() +"!\n");
                        curAcc = null;
                        return;
                    default://если неверный ввод
                        System.out.print("Invalid choice\n");
                }
            } catch (Exception e) {
                System.out.print("Error: " + e.getMessage()+"\n");
            }
        }
    }
    //поиск по транзакциям
    private static void search() {
        String type = null;
        while (true) {
            System.out.print("\nSearch transactions:\nType (1-deposit, 2-withdrawal, enter-skip):");//1-пополнение счета, 2-снятие со счета, ничего не введено-все операции
            String typeInput = scanner.nextLine().trim();
            if (typeInput.isEmpty()) {
                break;
            } else if (typeInput.equals("1")) {
                type = "DEPOSIT";
                break;
            } else if (typeInput.equals("2")) {
                type = "WITHDRAWAL";
                break;
            } else {
                System.out.print("Invalid input. Please enter 1, 2 or press Enter.\n");
            }
        }
        System.out.print("\nMin amount $: ");//минимальный порог, можно не задавать
        Double minAmount = isPosOrNull();
        System.out.print("\nMax amount $: ");//максимальный порог, можно не задавать
        Double maxAmount = isPosOrNull();
        //поиск по параметрам
        curAcc.searchTrans(type, minAmount, maxAmount);
    }
}import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//Транзакция(положить на счет/снять со счета) срдержит информацию о ее типе, сумме, времени и id
class Transaction {
    private static int nextId = 1;//общий счетчик транзакций
    private int id;
    private String type;
    private double amount;
    private LocalDateTime timestamp;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");//для вывода времени в красивом формате

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

//Банковский счет, содержит информацию о владельце, балансе и истории операций
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
    //корректность введенного пароля
    public boolean checkPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }
    //положить деньги на счет
    public void deposit(double amount) {
        if (!isActive) throw new RuntimeException("Account is not active");
        if (amount <= 0) throw new RuntimeException("Amount must be positive");

        balance += amount;
        trans.add(new Transaction("DEPOSIT", amount));
        System.out.printf("Deposited: $%.2f\n", amount);
    }
    //снять деньги со счета
    public void withdraw(double amount) {
        if (!isActive) throw new RuntimeException("Account is not active");
        if (amount <= 0) throw new RuntimeException("Amount must be positive");
        if (amount > balance) throw new RuntimeException("Not enough money");

        balance -= amount;
        trans.add(new Transaction("WITHDRAWAL", amount));
        System.out.printf("Withdrawn: $%.2f\n", amount);
    }
    //смотреть баланс
    public void showBalance() {
        if (!isActive) throw new RuntimeException("Account is not active");
        System.out.printf("\n balance: $%.2f\n", balance);
    }
    //история операций
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
    //поиск по транзакциям
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
    //геттеры для привятных полей: номер счета, имя владельца и баланс
    public String getAccNum() { return accNumber; }
    public String getUsername() { return username; }
    public double getBalance() { return balance; }
}
//Банковская система, открывается в начале
class BankingSystem {
    private static BankAccount curAcc = null;
    private static Scanner scanner = new Scanner(System.in);//сканирует ввод пользователя
    private static Map<String, BankAccount> accounts = new HashMap<>();//хранит все счета: номер счета-объект счета

    public static void main(String[] args) {
        System.out.print("BANK SYSTEM\n");

        while (true) {
            try {
                mainMenu();
                System.out.print("Select option: ");
                String input = scanner.nextLine().trim();//читаем ввод и убираем лишние пробелы

                if (input.isEmpty()) {
                    System.out.print("Please enter a number\n");
                    continue;
                }

                int choice = Integer.parseInt(input);
                //выбираем в меню доступные опции
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
                        System.out.print("Goodbye!\n");//при выходе
                        return;
                    default:
                        System.out.print("Invalid option\n");//если неверный ввод
                }
            } catch (Exception e) {
                System.out.print("Error: " + e.getMessage()+"\n");
            }
            System.out.print("\n");
        }
    }
    //опции
    private static void mainMenu() {
        System.out.print("1. Create account\n");
        System.out.print("2. Login\n");
        System.out.print("3. View all accounts\n");
        System.out.print("0. Exit\n");
    }
    //меню при входе в аккаунт
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
    //определяет, является ли сумма положительной
    private static double isPos() {
        while (true){
            try {
                String input = scanner.nextLine().trim();
                double value = Double.parseDouble(input.replace(',', '.'));//читаем сумму и с запятой, и с точкой
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
    //определяет, является ли число положительным, либо пустой ввод
    private static Double isPosOrNull() {
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return null;
        try {
            return Double.parseDouble(input.replace(',', '.'));//читаем сумму и с запятой, и с точкой
        }
        catch (NumberFormatException e) {
            System.out.print("\nInvalid number");
            return null;
        }
    }
    //проверка что имя пользователя состоит из символов
    private static boolean isValidUsername(String username) {
        if (username.isEmpty()) {
            return false;
        }

        for (int i = 0; i < username.length(); i++) {
            char c = username.charAt(i);//выделяем каждый символ
            if (!Character.isLetter(c)) {//смотрим, является ли он бувой
                return false;
            }
        }
        return true;
    }
    //проверка валидности пароля: состоит из 4 цифр
    private static boolean isValidPassword(String password) {
        if (password.length() != 4) {
            return false;
        }
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);//выделяем посимвольно
            if (!Character.isDigit(c)) {//если нашли не цифру
                return false;
            }
        }
        return true;
    }
    //создать новый аккаунт
    private static void createNewAcc() {
        System.out.print("\nCREATE NEW ACCOUNT\n");
        //читаем имя пользователя, пока не будет ввод только из символов
        String username;
        while (true) {
            System.out.print("Enter your name (letters only): ");
            username = scanner.nextLine().trim();

            if (username.isEmpty()) {
                System.out.print("Username can not be empty\n");
                continue;
            }
            if (!isValidUsername(username)) {
                System.out.print("Name must contain only letters\n");
                continue;
            }

            break;
        }
        //просим ввести пароль, пока не будет из 4 цифр
        String password;
        while (true) {
            System.out.print("Enter your password (4 digits): ");
            password = scanner.nextLine().trim();

            if (password.isEmpty()) {
                System.out.print("Password can not be empty\n");
                continue;
            }

            if (!isValidPassword(password)) {
                System.out.print("Password must be 4 digits\n");
                continue;
            }

            break;
        }
        //подтверждение пароля, проверка на их мэтч
        while (true) {
            System.out.print("Confirm password: ");
            String password2 = scanner.nextLine().trim();
            if (!password2.equals(password)) {
                System.out.print("Passwords are different\n");
                continue;
            }
            break;
        }
        String accNum = numberGenerator();//генерируем счет
        curAcc = new BankAccount(accNum, username, password);//создаем новый объект
        accounts.put(accNum, curAcc);//сохраняем немер счета и объект

        System.out.print("\nAccount created successfully!\n");
        System.out.print("Account number: " + accNum+"\n");
        System.out.print("Owner: " + username+"\n");
        accSession();
    }
    //для генерации десятизначного номера
    private static String numberGenerator() {
        Random random = new Random();
        String number = "";
        number += (1 + random.nextInt(9));
        for (int i = 0; i < 9; i++) {
            number += random.nextInt(10);
        }
        return number;
    }
    //вход в аккаунт
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
        //поиск аккаунта по номеру
        BankAccount account = accounts.get(accNum);
        if (account == null) {
            System.out.print("\nAccount not found");
            return;
        }
        //проверка пароля
        if (!account.checkPassword(password)) {
            System.out.print("\nWrong password");
            return;
        }

        curAcc = account;
        System.out.print("\nWelcome back, " + curAcc.getUsername() + "!\n");
        accSession();
    }
    //выводит список всех аккаунтов
    private static void allAcc() {
        System.out.print("\nAll accounts:\n");
        if (accounts.isEmpty()) {
            System.out.print("\nNo accounts\n");
            return;
        }
        //выводит информацию о счете в виде: номер | имя | баланс
        for (BankAccount acc : accounts.values()) {
            System.out.print(acc.getAccNum() + " | " + acc.getUsername() + " | $" + acc.getBalance()+"\n");
        }
    }
    //работа после авторизации
    private static void accSession() {
        while (true) {
            try {
                accMenu();
                System.out.print("\nChoose action: ");
                String input = scanner.nextLine().trim();
                //если пустой ввод
                if (input.isEmpty()) {
                    System.out.print("Please select an option\n");
                    continue;
                }

                int choice = Integer.parseInt(input);
                //выбор операции
                switch (choice) {
                    case 1://пополнение
                        System.out.print("\nEnter deposit amount: $");
                        double dep = isPos();
                        curAcc.deposit(dep);
                        break;
                    case 2://снятие
                        System.out.print("\nEnter withdrawal amount: $");
                        double wthd = isPos();
                        curAcc.withdraw(wthd);
                        break;
                    case 3://баланс
                        curAcc.showBalance();
                        break;
                    case 4://история операций
                        curAcc.transHist();
                        break;
                    case 5://поиск по транзакциям
                        search();
                        break;
                    case 6://выход из аккаунта
                        System.out.print("See you soon," + curAcc.getUsername() +"!\n");
                        curAcc = null;
                        return;
                    default://если неверный ввод
                        System.out.print("Invalid choice\n");
                }
            } catch (Exception e) {
                System.out.print("Error: " + e.getMessage()+"\n");
            }
        }
    }
    //поиск по транзакциям
    private static void search() {
        String type = null;
        while (true) {
            System.out.print("\nSearch transactions:\nType (1-deposit, 2-withdrawal, enter-skip):");//1-пополнение счета, 2-снятие со счета, ничего не введено-все операции
            String typeInput = scanner.nextLine().trim();
            if (typeInput.isEmpty()) {
                break;
            } else if (typeInput.equals("1")) {
                type = "DEPOSIT";
                break;
            } else if (typeInput.equals("2")) {
                type = "WITHDRAWAL";
                break;
            } else {
                System.out.print("Invalid input. Please enter 1, 2 or press Enter.\n");
            }
        }
        System.out.print("\nMin amount $: ");//минимальный порог, можно не задавать
        Double minAmount = isPosOrNull();
        System.out.print("\nMax amount $: ");//максимальный порог, можно не задавать
        Double maxAmount = isPosOrNull();
        //поиск по параметрам
        curAcc.searchTrans(type, minAmount, maxAmount);
    }
}
