import com.sun.jdi.Value;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.security.Key;
import java.util.*;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.nio.file.Path;


public class Main {
    public static void main(String[] args) throws IOException {



        String input = """
                n = 10
                sum = 0
                i = 1
                while i <= n
                    sum += i
                    i += 1
                end
                """;

        Path path = Path.of("file.txt");
        List<String> lines = Files.readAllLines(path);

        lines.forEach(l -> {
            System.out.println(Statement.getStatement(l));
        });

        
        Scanner scanner = new Scanner(input);

        String[] Lines = input.split("\n");
        System.out.println(Arrays.toString(Lines));
        HashMap<String, Integer> save = new HashMap<>();

        reverseArray(Lines);
        for (String line : lines) {
            line = line.strip();
            if (line.isEmpty()) continue;

            // Handle variable assignment
            if (line.contains("=")) {
                handleAssignment(line, save);
            }

            // Handle while loops
            else if (line.startsWith("while")) {
                handleWhile(line, save);
            }
        }
        Stack<String> orderedLines = new Stack<>();
        orderedLines.addAll(Arrays.asList(Lines));

        System.out.println(Arrays.toString(Lines));

//        while (scanner.hasNextLine()) {
//            String line = scanner.nextLine();
//            Lines.add(line);
//        }
        System.out.println(save);


    }

    //handleAssignment
    static void handleAssignment(String line, Map save) {
        boolean assigning = false;
        String Var = "";
        String Val = "";
        for (char i : line.toCharArray()) {
            if (i == ' ') continue;
            else if (Character.isDigit(i) && assigning) Val += 1;
            else if (i <= 'z' && i >= 'a' || i == '_') {
                Var += i;
            }
            else if (i == '=')
            {
                save.put(Var, null);
                assigning = true;
            }
            else if (i == '\n') {
                int newVal = Integer.parseInt(Val);
                save.put(Var, newVal);
                break;
            }
        }
    }

    //handleWhile
    static void handleWhile(String line, Map save) {
        line.replace("while", "");
        String Var = "";
        String Val = "";
        Boolean assigning = false;
        for (char i : line.toCharArray()) {
            if (i == ' ') continue;
            else if (Character.isDigit(i) && assigning) Val += i;
            else if (i <= 'z' && i >= 'a' || i == '_') {
                Var += i;

            } else if (i == '=') {
                assigning = true;
                save.put(Var, null);
            }
        }
        int newVal = Integer.parseInt(Val);
        save.put(Var, newVal);
    }

    // Reverses a string array
    static String[] reverseArray(String[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            String temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
        return array;
    }
}

enum Statement {

    ASSIGNMENT("\\w+ \\s*(=|-=|\\+=|%)\\s*\\w+"),
    IF("if +(\\w+|\\d+) *(< |> |== |<= |>= |!= ) *(\\w+|\\d+)"),
    ELSE("else"),
    WHILE("while +(\\w+|\\d+) *(< |> |== |<= |>= |!= ) *(\\w+|\\d+)"),
    END("end"),
    CONDITION(".*(==|!=|<=|>=|<|>).*"),
    PRINT("puts +(\\w+|\\d+)"),
    BLANK(" *");

    private final Pattern pattern;

    Statement(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public boolean matches(String code) {
        return pattern.matcher(code).find();
    }


    public static Statement getStatement(String code) {
        for (Statement s : values()) {
            if (s.matches(code)) {
                return s;
            }
        }
        return null;
    }
}
