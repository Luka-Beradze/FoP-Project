package Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.*;

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

    public static void main(String[] args) throws assignmentException{

        for (int i = lines.size() - 1; i >= 0; i--) {
            String line = lines.get(i);
            if (Statement.addToStack(line)) {
                runningStack.push(line.strip());
            }
        }
        System.out.println(runningStack);

        runAlgorithm(lines, runningStack, variableMap); // run's algorithm
    }

    private static void runAlgorithm(List<String> currentLines, Stack<String> currentRunningStack, Map<String, Object> currentVariableMap) throws assignmentException{

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
                    int indexWhile = currentLines.indexOf(currentRunningStack.getLast());

                    createWhileLoop whileLoop = new createWhileLoop(indexWhile, currentLines, currentVariableMap);

                    // update variables
                    whileLoop.whileVariableMap.forEach((key, value) -> {
                        if (currentVariableMap.containsKey(key)) {
                            currentVariableMap.put(key, value);
                        }
                    });

                    currentRunningStack.pop();
                    break;
                case FOR:
                    currentRunningStack.pop();
                    break;
                case END:
                    currentRunningStack.pop();
                    break;
                case PRINT:
                    int indexPrint = currentLines.indexOf(currentRunningStack.getLast());

                    runPrint(indexPrint, currentLines, currentVariableMap);

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
    
    public static void parseKeyValuePair(String code_line, Map<String, Object> currentVariableMap) throws assignmentException{
        Matcher matcher = Statement.ASSIGNMENT.getPattern().matcher(code_line);
        if (matcher.find()) {
            String key = matcher.group(1);
            String operator = matcher.group(2);
            // split assignment and store an expression side in valueExpression
            String[] parts = matcher.group(0).split(Helper.ASSIGNMENT_HELPER.getPattern().toString());
            String valueExpression = parts[1].strip();
            Object evaluatedValue;

            // Assign value
            try {
                evaluatedValue = Long.valueOf(valueExpression);
            } catch (NumberFormatException e) {
                // if variable, assign its value
                if (currentVariableMap.containsKey(valueExpression)) {
                    evaluatedValue = currentVariableMap.get(valueExpression);
                } else if (valueExpression.equals("true") || valueExpression.equals("false")) {
                    // else if boolean;
                    evaluatedValue = Boolean.valueOf(valueExpression);
                } else {
                    // replace variables with values in an expression
                    for (Map.Entry<String, Object> entry : currentVariableMap.entrySet()) {
                        String mapKey = entry.getKey();
                        Object mapValue = entry.getValue();

                        // Ensure the value is a Long before replacing
                        if (mapValue instanceof Long) {
                            valueExpression = valueExpression.replaceAll("\\b" + mapKey + "\\b", mapValue.toString());
                        }
                    }
                    System.out.println(valueExpression);
                    // Evaluate an expression
                    evaluatedValue = expressionEvaluation.evaluate(valueExpression);
//                    evaluatedValue = evaluateExpression(valueExpression, currentVariableMap);
                }
            }

            // Handle compound assignment operators
            if (currentVariableMap.containsKey(key) && evaluatedValue instanceof Long) {
                Long currentValue = (Long) currentVariableMap.get(key);
                Long numericValue = (Long) evaluatedValue;

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
                        currentVariableMap.put(key, evaluatedValue);
                        break;
                }
            } else {
                // Simple assignment for non-numeric values or if key doesn't exist
                currentVariableMap.put(key, evaluatedValue);
            }
        } else {
            System.out.println("Could not parseKeyValuePair"); // might make this throw an exception later
        }
    }




    public static void runPrint(int index, List<String> currentLines, Map<String, Object> currentVariableMap) {
        String printLine = currentLines.get(index).strip();
        Matcher matcher = Statement.PRINT.getPattern().matcher(printLine);
        String print = "";

        if (matcher.find()) {
            if (currentVariableMap.containsKey(matcher.group(1))){
                print = currentVariableMap.get(matcher.group(1)).toString();
            } else {
                print = matcher.group(1);
            }
            System.out.println(print);
        } else {
            System.out.println("Invalid Print Statement"); // might become exception
        }
    }

    static class createWhileLoop {

        List<String> whileLines = new ArrayList<>();
        Stack<String> whileStack = new Stack<>();
        Map<String, Object> whileVariableMap = new HashMap<>();

        int indexWhile;

        createWhileLoop(int index, List<String> currentLines, Map<String, Object> currentVariableMap) throws assignmentException{
            indexWhile = index;
            whileVariableMap.putAll(currentVariableMap);
            int i = index + 1; // get next line after while
            while (!Objects.equals(currentLines.get(i), "end")){
                whileLines.add(currentLines.get(i).substring(2));
                i++;
            }

            for (int j = whileLines.size() - 1; j >= 0; j--) {
                String line = whileLines.get(j);
                if (Statement.addToStack(line)) {
                    whileStack.push(line.strip());
                }
            }

            while (condition_is_met(currentLines)){
                Stack<String> loopStack = new Stack<>();
                loopStack.addAll(whileStack);
                runAlgorithm(whileLines, loopStack, whileVariableMap);
            }
        }

        public boolean condition_is_met(List<String> currentLines){
            // parse condition part
            String whileLine = currentLines.get(indexWhile).strip();
            Matcher matcher = Statement.WHILE.getPattern().matcher(whileLine);
            String Condition = "";

            if (matcher.find()) {
                Condition = matcher.group(1);
            }else {
                System.out.println("Invalid While Line");
                return false;
            }
            // parse condition part

            Matcher conditionMatcher = Helper.CONDITION.getPattern().matcher(Condition);

            if (conditionMatcher.find()) {
                if (Helper.CONDITION.getPattern().toString().matches(".*[><=].*")){
                    String leftOperand = conditionMatcher.group(1);
                    String comparator = conditionMatcher.group(2);
                    String rightOperand = conditionMatcher.group(3);

                    // if operands are variables, get their value
                    if (whileVariableMap.containsKey(leftOperand)){
                        leftOperand = whileVariableMap.get(leftOperand).toString();
                    }
                    if (whileVariableMap.containsKey(rightOperand)){
                        rightOperand = whileVariableMap.get(rightOperand).toString();
                    }

                    return switch (comparator) {
                        case "==" -> Long.parseLong(leftOperand) == Long.parseLong(rightOperand);
                        case "!=" -> Long.parseLong(leftOperand) != Long.parseLong(rightOperand);
                        case "<" -> Long.parseLong(leftOperand) < Long.parseLong(rightOperand);
                        case ">" -> Long.parseLong(leftOperand) > Long.parseLong(rightOperand);
                        case "<=" -> Long.parseLong(leftOperand) <= Long.parseLong(rightOperand);
                        case ">=" -> Long.parseLong(leftOperand) >= Long.parseLong(rightOperand);
                        default -> false;
                    };

                } else if (whileVariableMap.containsKey(matcher.group(1))){
                    return Boolean.parseBoolean(whileVariableMap.get(matcher.group(1)).toString()); // if variable isn't boolean, returns false
                } else {
                    return Boolean.parseBoolean(conditionMatcher.group(1));
                }
            } else {
                System.out.println("Invalid Condition"); // also might become exception
            }
            return false;
        }

    }

}

enum Statement {

    ASSIGNMENT("([a-z_][a-zA-Z0-9_]*)\\s*(\\+=|-=|\\*=|\\/=|=|%=)\\s*([a-z_][a-zA-Z0-9_]*|\\d+|\\s*[\\+\\-\\*\\/%]?\\s*([a-z_][a-zA-Z0-9_]*|\\d+|\\([^\\)]+\\))\\s*)+\\s*$"),
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
