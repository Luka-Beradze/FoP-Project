package Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Interpreter {

    private static final String algorithm = "src/Algorithms/SumOfN.txt";

    public static Path path = Path.of(algorithm);
    public static List<String> lines;
    static {
        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stack<String> runningStack = new Stack<>();
    public static Map<String, Object> variableMap = new HashMap<>();

    public static void main(String[] args){


        for (int i = lines.size() - 1; i >= 0; i--) {
            String line = lines.get(i);
            if (Statement.addToStack(line)) {
                runningStack.push(line.strip());
            }
        }
        System.out.println(runningStack);

        runAlgorithm(lines, runningStack, variableMap); // run's algorithm
    }

    private static void runAlgorithm(List<String> currentLines, Stack<String> currentRunningStack, Map<String, Object> currentVariableMap) {

        while (!currentRunningStack.empty()){
            switch (Statement.getStatement(currentRunningStack.getLast())){
                case null:
                    break;
                case ASSIGNMENT:
                    parseKeyValuePair(currentRunningStack.pop(), currentVariableMap);
                    break;
                case IF:
                    currentRunningStack.pop();
                    break;
                case ELSE:
                    currentRunningStack.pop();
                    break;
                case WHILE:
                    currentRunningStack.pop();
                    break;
                case FOR:
                    currentRunningStack.pop();
                    break;
                case END:
                    currentRunningStack.pop();
                    break;
                case PRINT:
                    currentRunningStack.pop();
                    break;
                case SCOPE:
                    currentRunningStack.pop();
                    break;
                case EMPTY:
                    currentRunningStack.pop();
                    break;
            }
        }
    }
}




enum Statement {

    ASSIGNMENT("([a-z_][a-zA-Z0-9_]*)\\s*(\\+=|-=|\\*=|\\/=|=|%=|=)\\s*(\\d+|true|false|[a-z_][a-zA-Z0-9_]*)$"),
    // ASSIGNMENT Group 1: Variable Name; Group 2: Operator; Group 3: Variable Value;
    IF("if .*"),
    ELSE("else"),
    WHILE("while \\s*(" + Helper.CONDITION.getPattern().toString() + ")\\s*$"),
    FOR("for .*"),
    END("end"),
    PRINT("puts ([a-z_][a-zA-Z0-9_]*|\\d+)$"),
    // PRINT Group 1: item to print out;
    SCOPE("  .*"),
    EMPTY("");

    private final Pattern pattern;

    Statement(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public Pattern getPattern() {
        return pattern;
    }

    public boolean matches(String code) {
        return pattern.matcher(code).find();
    }

    public static boolean addToStack(String code_line) {
        if (getStatement(code_line) == ASSIGNMENT ||
                getStatement(code_line) == IF ||
                getStatement(code_line) == WHILE ||
                getStatement(code_line) == FOR ||
                getStatement(code_line) == PRINT ||
                getStatement(code_line) == END) {
            return true;
        }
        return false;
    }


    public static Statement getStatement(String code_line) {
        for (Statement s : values()) {
            if (Pattern.matches(s.pattern.toString(), code_line)) {
                return s;
            }
        }
        return null;
    }
}

enum Helper {

    CONDITION("([a-z_][a-zA-Z0-9_]*|\\d+)\\s*(==|!=|<=|>=|<|>)\\s*([a-z_][a-zA-Z0-9_]*|\\d+)$|true|false|[a-z_][a-zA-Z0-9_]*"),// Does not contain "!" before a condition
    // CONDITION Group 1: left operand; Group 2: comparator; Group 3: right operand.
    ASSIGNMENT_HELPER("\\+=|-=|\\*=|\\/=|=|%=|=");

    private final Pattern pattern;

    Helper(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public Pattern getPattern() {
        return pattern;
    }
}
