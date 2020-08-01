import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Flashcards {
    private final String SEPARATOR = "---";

    private Scanner scanner;
    private Map<String, String> cards;
    private Map<String, Integer> errorCards;
    private List<String> logs;

    public void menu(String[] args) {
        String userAction = "";
        while (!userAction.equals("exit")) {
            consoleOutput("\nInput the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):");
            userAction = consoleInput();

            switch (userAction) {
                case "add":
                    add();
                    break;
                case "remove":
                    remove();
                    break;
                case "import":
                    flashImport();
                    break;
                case "export":
                    flashExport();
                    break;
                case "ask":
                    ask();
                    break;
                case "exit":
                    exit(args);
                    break;
                case "log":
                    log();
                    break;
                case "hardest card":
                    hardestCard();
                    break;
                case "reset stats":
                    resetStats();
                    break;
            }
        }
    }

    private void add() {
        consoleOutput("The card:");
        String card = consoleInput();
        if (cards.containsKey(card)) {
            consoleOutput("The card \"" + card + "\" already exists.");
            return;
        }

        consoleOutput("The definition of the card:");
        String definition = consoleInput();
        if (cards.containsValue(definition)) {
            consoleOutput("The definition \"" + definition + "\" already exists.");
            return;
        }

        cards.put(card, definition);
        consoleOutput("The pair (\"" + card + "\":\"" + definition + "\") has been added.");
    }
    private void remove() {
        consoleOutput("The card:");
        String card = consoleInput();
        if (!cards.containsKey(card)) {
            consoleOutput("Can't remove \"" + card + "\": there is no such card.");
        }
        else {
            cards.remove(card);
            if (errorCards.containsKey(card)) errorCards.remove(card);
            consoleOutput("The card has been removed.");
        }

    }

    private void flashImport( ) {
        consoleOutput("File name:");
        String fileName = consoleInput();
        flashImport(new File(fileName));
    }
    private void flashImport(File file) {
        try (Scanner scannerFile = new Scanner(file)) {
            int count = 0;
            while (scannerFile.hasNext()) {
                String pair[] = scannerFile.nextLine().split(SEPARATOR);
                cards.put(pair[0], pair[1]);
                if (Integer.parseInt(pair[2]) != 0) errorCards.put(pair[0], Integer.parseInt(pair[2]));
                count++;
            }
            consoleOutput( count + " cards have been loaded.");

        } catch (FileNotFoundException e) {
            consoleOutput("File not found.");
        }
    }

    private void flashExport() {
        consoleOutput("File  name:");
        String fileName = consoleInput();
        flashExport(new File(fileName));
    }
    private void flashExport(File file) {
        try (PrintWriter printWriter = new PrintWriter(file)) {
            for (String card : cards.keySet()) {
                String numberError = errorCards.containsKey(card) ? errorCards.get(card).toString() : "0";
                printWriter.println(card + SEPARATOR + cards.get(card) + SEPARATOR + numberError);
            }
            consoleOutput(cards.size() + " cards have been saved.");
        } catch (IOException e) {
            consoleOutput("An exception occurs " + e.getMessage());
        }
    }

    private void ask() {
        Random random = new Random();

        List<String> cardsList = new ArrayList<String>(cards.keySet());

        consoleOutput("How many times to ask?");
        int times = Integer.parseInt(consoleInput());

        for (int i = 0; i < times; i++) {
            String randomCard = cardsList.get(random.nextInt(cardsList.size()));

            consoleOutput("Print the definition of \"" + randomCard + "\":");
            String rightAnswer = cards.get(randomCard);
            String answer = consoleInput();
            if (answer.equals(rightAnswer)) consoleOutput("Correct answer.");
            else {
                errorCards.put(randomCard, errorCards.getOrDefault(randomCard, 0) + 1);

                if (cards.containsValue(answer)) {
                    consoleOutput("Wrong answer. The correct one is \"" + rightAnswer + "\", you've just written the definition of \"" + getKeyByValue(cards, answer) + "\".");
                }
                else {
                    consoleOutput("Wrong answer. The correct one is \"" + rightAnswer + "\".");
                }
            }
        }
    }
    private String getKeyByValue(Map<String, String> map ,String Value) {
        for (String key : map.keySet()) {
            if (Value.equals(map.get(key)))  return key;
        }
        return null;
    }

    private void exit() {
        consoleOutput("Bye, bye!");
    }
    private void exit(String[] args) {
        exit();
        exportCLI(args);
    }

    private void log() {
        consoleOutput("File name");
        String fileName = consoleInput();

        File file = new File(fileName);
        try (PrintWriter printWriter = new PrintWriter(file)) {
            for (String log : logs) {
                printWriter.println(log);
            }
            consoleOutput("The log has been saved");
        } catch (IOException e) {
            consoleOutput("An exception occurs " + e.getMessage());
        }
    }

    private void consoleOutput(String str) {
        logs.add(str);
        System.out.println(str);
    }
    private String consoleInput() {
        String line = scanner.nextLine();
        logs.add(line);
        return line;
    }

    private void hardestCard() {
        List<String> hardestCards = new ArrayList<>();
        int numberError = 0;

        for (String card : errorCards.keySet()) {
            Integer cardError = errorCards.get(card);
            if (cardError > numberError) {
                numberError = cardError;
                hardestCards.clear();
                hardestCards.add(card);
            }
            else if (cardError == numberError) {
                hardestCards.add(card);
            }
        }

        if (numberError == 0) consoleOutput( "There are no cards with errors.");

        else {
            String forConsole = "";
            for (int i = 0; i < hardestCards.size(); i++) {
                if (i != hardestCards.size() - 1)
                    forConsole += "\"" + hardestCards.get(i) + "\", ";
                else {
                    forConsole += "\"" + hardestCards.get(i) + "\".";
                }
            }
            String s = hardestCards.size() > 1 ? "s are " : " is ";
            consoleOutput("The hardest card" + s + forConsole + "You have " + numberError + " answering them.");
        }
    }

    private void resetStats() {
        errorCards.clear();
        consoleOutput("Card statistics has been reset.");
    }

    Flashcards() {
        cards = new LinkedHashMap<String, String>();
        logs = new ArrayList<String>();
        scanner = new Scanner(System.in);
        errorCards = new HashMap<>();
    }
    Flashcards(String[] args) {
        this();
        importCLI(args);
    }

    private void importCLI(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-import")) flashImport(new File(args[i+1]));
        }
    }
    private void exportCLI(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-export")) flashExport(new File(args[i+1]));
        }
    }
}

