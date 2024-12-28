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


    public static void parseKeyValuePair(String code_line, Map<String, Object> currentVariableMap) {
        Matcher matcher = Statement.ASSIGNMENT.getPattern().matcher(code_line);
        if (matcher.find()) {
            String key = matcher.group(1);
            String operator = matcher.group(2);
            Object value;

            // Assign value
            try {
                value = Long.valueOf(matcher.group(3));
            } catch (NumberFormatException e) {
                // if variable, assign its value
                if (currentVariableMap.containsKey(matcher.group(3))) {
                    value = currentVariableMap.get(matcher.group(3));
                } else {
                    // else boolean; Only false or true reaches this point.
                    value = Boolean.valueOf(matcher.group(3));
                }
            }

            // Handle compound assignment operators
            if (currentVariableMap.containsKey(key) && value instanceof Long) {
                Long currentValue = (Long) currentVariableMap.get(key);
                Long numericValue = (Long) value;
 
                switch (operator) {
                    case "+=":
                        currentVariableMap.put(key, currentValue + numericValue);
                        break;
                    case "-=":
                        currentVariableMap.put(key, currentValue - numericValue);
                        break;
                    case "*=":
                        currentVariableMap.put(key, currentValue * numericValue);
                        break;
                    case "/=":
                        if (numericValue != 0) {
                            currentVariableMap.put(key, currentValue / numericValue);
                        } else {
                            throw new ArithmeticException("Division by zero");
                        }
                        break;
                    case "%=":
                        if (numericValue != 0) {
                            currentVariableMap.put(key, currentValue % numericValue);
                        } else {
                            throw new ArithmeticException("Division by zero");
                        }
                        break;
                    default: // Simple assignment
                        currentVariableMap.put(key, value);
                        break;
                }
            } else {
                // Simple assignment for non-numeric values or if key doesn't exist
                currentVariableMap.put(key, value);
            }
        } else {
            System.out.println("Could not parseKeyValuePair"); // might make this throw an exception later
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
