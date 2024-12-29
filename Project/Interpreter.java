package Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Main body of an interpreter program.
public class Interpreter {

    private static final String algorithm = "src/Algorithms/SumOfN.txt"; // Points to a file containing the Ruby algorithm to be interpreted.

    public static Path path = Path.of(algorithm); // creates a Path object pointing to the file.
    public static List<String> lines;
    //This file is read and stored in a list called "lines".
    static {
        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            throw new RuntimeException(e); // An error will accure if pointed file does not exist or cannot be opened.
        }
    }
    // A stack that stores code lines to be executed.
    public static Stack<String> runningStack = new Stack<>();
    // A map which stores variable names and their values for the ruby code.
    public static Map<String, Object> variableMap = new HashMap<>();

    public static void main(String[] args) throws AssignmentExceptions, SyntaxError{
        // Itterate over lines.
        for (int i = lines.size() - 1; i >= 0; i--) {
            String line = lines.get(i);
            if (Statement.addToStack(line)) {
                runningStack.push(line.strip()); // Add valid lines to runningStack.
            }
        }

        runAlgorithm(lines, runningStack, variableMap); // run's algorithm.

    }

    private static void runAlgorithm(List<String> currentLines, Stack<String> currentRunningStack, Map<String, Object> currentVariableMap) throws AssignmentExceptions, SyntaxError{
        // Iterate over lines as long as there are items in currentRunningStack.
        while (!currentRunningStack.empty()){
            // Determine what type of statement the line is.
            switch (Statement.getStatement(currentRunningStack.getLast())){
                case ASSIGNMENT:
                    // store the variable key-value pair in currentVariableMap.
                    parseKeyValuePair(currentRunningStack.pop(), currentVariableMap);
                    break;
                case IF:
                    // get if line index in currentLines list.
                    int indexIf = currentLines.indexOf(currentRunningStack.getLast());
                    // run inner scope of if statement.
                    createIf ifStatement = new createIf(indexIf, currentLines, currentVariableMap);

                    // update variables.
                    ifStatement.ifVariableMap.forEach((key, value) -> {
                        if (currentVariableMap.containsKey(key)) {
                            currentVariableMap.put(key, value);
                        }
                    });
                    
                    currentRunningStack.pop();
                    break;
                case ELSE:
                    // get else line index in currentLines list.
                    int indexElse = currentLines.indexOf(currentRunningStack.getLast());
                    // run inner scope of else statement.
                    createElse elseStatement = new createElse(indexElse, currentLines, currentVariableMap);

                    // update variables
                    elseStatement.elseVariableMap.forEach((key, value) -> {
                        if (currentVariableMap.containsKey(key)) {
                            currentVariableMap.put(key, value);
                        }
                    });

                    currentRunningStack.pop();
                    break;
                case WHILE:
                    // get else line index in currentLines list.
                    int indexWhile = currentLines.indexOf(currentRunningStack.getLast());
                    // run inner scope of a while loop.
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
                    // get puts line index in currentLines list.
                    int indexPrint = currentLines.indexOf(currentRunningStack.getLast());
                    // print out to the console.
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

    // parses Key-Value pair for variable assignments.
    public static void parseKeyValuePair(String code_line, Map<String, Object> currentVariableMap) throws AssignmentExceptions {
        // initialize a Matcher object to apply a regex pattern to inputted code_line.
        Matcher matcher = Statement.ASSIGNMENT.getPattern().matcher(code_line);
        if (matcher.find()) {
            String key = matcher.group(1); // The variable name
            String operator = matcher.group(2); // The operator
            // split assignment and store an expression side in valueExpression
            String[] parts = matcher.group(0).split(Helper.ASSIGNMENT_HELPER.getPattern().toString());
            String valueExpression = parts[1].strip();
            Object evaluatedValue; // The final value

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
                } else { // else it's an expression.
                    // replace variables with values in an expression.
                    for (Map.Entry<String, Object> entry : currentVariableMap.entrySet()) {
                        String mapKey = entry.getKey();
                        Object mapValue = entry.getValue();

                        // Ensure the value is a Long before replacing
                        if (mapValue instanceof Long) {
                            valueExpression = valueExpression.replaceAll("\\b" + mapKey + "\\b", mapValue.toString());
                        }
                    }
                    // Evaluate an expression
                    evaluatedValue = expressionEvaluation.evaluate(valueExpression);
                }
            }

            // Handle compound assignment operators, if variable already exists in currentVariableMap and has value type Long.
            if (currentVariableMap.containsKey(key) && evaluatedValue instanceof Long) {
                Long currentValue = (Long) currentVariableMap.get(key);
                Long numericValue = (Long) evaluatedValue;
                // choose operation to execute depending on the operator.
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
                            // if division by 0 detected throw an ArithmeticException.
                            throw new ArithmeticException("Division by zero");
                        }
                        break;
                    case "%=":
                        if (numericValue != 0) {
                            currentVariableMap.put(key, currentValue % numericValue);
                        } else {
                            // if division by 0 detected throw an ArithmeticException.
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
            // If invalid assignment line throw an exception.
            throw new AssignmentExceptions("Invalid Assignment Statement");
        }
    }

    // print out to the console for "puts" method in ruby.
    public static void runPrint(int index, List<String> currentLines, Map<String, Object> currentVariableMap) throws SyntaxError {
        // get print line.
        String printLine = currentLines.get(index).strip();
        // match the print statement pattern.
        Matcher matcher = Statement.PRINT.getPattern().matcher(printLine);
        String print = ""; // variable to hold what should be printed.

        if (matcher.find()) {
            // check if the value is a variable
            if (currentVariableMap.containsKey(matcher.group(1))){
                // if it's a variable, get its value.
                print = currentVariableMap.get(matcher.group(1)).toString();
            } else {
                // else use it directly.
                print = matcher.group(1);
            }
            System.out.println(print); // pretty self-explanatory
        } else {
            // if PRINT pattern didn't match, it's an invalid print line.
            throw new SyntaxError("Invalid Print Statement");
        }
    }

    // class for running while loops.
    static class createWhileLoop {

        List<String> whileLines = new ArrayList<>(); // stores lines inside the while loop
        Stack<String> whileStack = new Stack<>(); // stores the loop's statements for execution.
        Map<String, Object> whileVariableMap = new HashMap<>(); // keeps track of variable values during loop execution.

        int indexWhile; // index where the while loop starts in the currentLines code.

        createWhileLoop(int index, List<String> currentLines, Map<String, Object> currentVariableMap) throws AssignmentExceptions, SyntaxError {
            indexWhile = index;
            whileVariableMap.putAll(currentVariableMap); // Copy the outer scope variables to the while loop scope.
            int i = index + 1; // get next line after while.
            // add inner while loop code to the whileLines.
            while (!Objects.equals(currentLines.get(i), "end")){
                whileLines.add(currentLines.get(i).substring(2));
                i++;
            }
            // add loop statements to the stack for execution.
            for (int j = whileLines.size() - 1; j >= 0; j--) {
                String line = whileLines.get(j);
                if (Statement.addToStack(line)) {
                    whileStack.push(line.strip());
                }
            }
            // Execute the loop as long as the condition is met.
            while (condition_is_met(currentLines)){
                Stack<String> loopStack = new Stack<>(); // new stack for the loop iteration.
                loopStack.addAll(whileStack);
                runAlgorithm(whileLines, loopStack, whileVariableMap); // run while loop code.
            }
        }

        // Checks if the given condition is met.
        public boolean condition_is_met(List<String> currentLines) throws SyntaxError{
            // parse condition part
            String whileLine = currentLines.get(indexWhile).strip();
            Matcher matcher = Statement.WHILE.getPattern().matcher(whileLine);
            String Condition = "";

            if (matcher.find()) {
                Condition = matcher.group(1);
            }else {
                // if WHILE didn't match throw an exception.
                throw new SyntaxError("Invalid While Statement");
            }
            // parse condition part

            // Matches the condition.
            Matcher conditionMatcher = Helper.CONDITION.getPattern().matcher(Condition);

            if (conditionMatcher.find()) {
                // check if the condition contains any of the relational operators.
                if (Helper.CONDITION.getPattern().toString().matches(".*[><=].*")){
                    // exctract from the condition.
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
                    // compare the values depending on the comparator.
                    return switch (comparator) {
                        case "==" -> Long.parseLong(leftOperand) == Long.parseLong(rightOperand);
                        case "!=" -> Long.parseLong(leftOperand) != Long.parseLong(rightOperand);
                        case "<" -> Long.parseLong(leftOperand) < Long.parseLong(rightOperand);
                        case ">" -> Long.parseLong(leftOperand) > Long.parseLong(rightOperand);
                        case "<=" -> Long.parseLong(leftOperand) <= Long.parseLong(rightOperand);
                        case ">=" -> Long.parseLong(leftOperand) >= Long.parseLong(rightOperand);
                        default -> false;
                    };
                // if the variable has a boolean value
                } else if (whileVariableMap.containsKey(matcher.group(1))){
                    // if variable isn't boolean, returns false
                    return Boolean.parseBoolean(whileVariableMap.get(matcher.group(1)).toString());
                } else {
                    // else it is a true or false directly.
                    return Boolean.parseBoolean(conditionMatcher.group(1));
                }
            } else {
                // else it means that the condition is not valid.
                throw new SyntaxError("Invalid While Condition");
            }
        }

    }

    // class for running if statements.
    static class createIf {

        List<String> ifLines = new ArrayList<>(); // stores lines inside the if statement.
        Stack<String> ifStack = new Stack<>(); // stores the if statement lines for execution.
        Map<String, Object> ifVariableMap = new HashMap<>(); // keeps track of variable values during if execution.

        int indexIf; // index where the if statement starts in the currentLines code.

        createIf(int index, List<String> currentLines, Map<String, Object> currentVariableMap) throws AssignmentExceptions, SyntaxError {
            indexIf = index;
            ifVariableMap.putAll(currentVariableMap); // Copy the outer scope variables to the if statement scope.
            int i = index + 1; // get next line after if
            // add inner if statement code to the ifLines.
            while (!Objects.equals(currentLines.get(i), "end") && !Objects.equals(currentLines.get(i), "else")){
                ifLines.add(currentLines.get(i).substring(2));
                i++;
            }
            // add if statement lines to the stack for execution.
            for (int j = ifLines.size() - 1; j >= 0; j--) {
                String line = ifLines.get(j);
                if (Statement.addToStack(line)) {
                    ifStack.push(line.strip());
                }
            }
            // Execute the lines if if statement condition is true;
            if (condition_is_met(currentLines)) {
                Stack<String> ifInnerStack = new Stack<>(); // new stack for the if statement lines.
                ifInnerStack.addAll(ifStack);
                runAlgorithm(ifLines, ifInnerStack, ifVariableMap); // run if statement code.
            }
        }
        
        // Checks if the given condition is met.
        public boolean condition_is_met(List<String> currentLines) throws SyntaxError{
            // parse condition part
            String whileLine = currentLines.get(indexIf).strip();
            Matcher matcher = Statement.IF.getPattern().matcher(whileLine);
            String Condition = "";

            if (matcher.find()) {
                Condition = matcher.group(1);
            }else {
                // if IF didn't match throw an exception.
                throw new SyntaxError("Invalid If Statement");
            }
            // parse condition part

            // Matches the condition.
            Matcher conditionMatcher = Helper.CONDITION.getPattern().matcher(Condition);

            if (conditionMatcher.find()) {
                 // check if the condition contains any of the relational operators.
                if (Helper.CONDITION.getPattern().toString().matches(".*[><=].*")){
                    // exctract from the condition.
                    String leftOperand = conditionMatcher.group(1);
                    String comparator = conditionMatcher.group(2);
                    String rightOperand = conditionMatcher.group(3);

                    // if operands are variables, get their value
                    if (ifVariableMap.containsKey(leftOperand)){
                        leftOperand = ifVariableMap.get(leftOperand).toString();
                    }
                    if (ifVariableMap.containsKey(rightOperand)){
                        rightOperand = ifVariableMap.get(rightOperand).toString();
                    }
                    // compare the values depending on the comparator.
                    return switch (comparator) {
                        case "==" -> Long.parseLong(leftOperand) == Long.parseLong(rightOperand);
                        case "!=" -> Long.parseLong(leftOperand) != Long.parseLong(rightOperand);
                        case "<" -> Long.parseLong(leftOperand) < Long.parseLong(rightOperand);
                        case ">" -> Long.parseLong(leftOperand) > Long.parseLong(rightOperand);
                        case "<=" -> Long.parseLong(leftOperand) <= Long.parseLong(rightOperand);
                        case ">=" -> Long.parseLong(leftOperand) >= Long.parseLong(rightOperand);
                        default -> false;
                    };
                // if the variable has a boolean value
                } else if (ifVariableMap.containsKey(matcher.group(1))){
                    // if variable isn't boolean, returns false
                    return Boolean.parseBoolean(ifVariableMap.get(matcher.group(1)).toString()); // if variable isn't boolean, returns false
                } else {
                    // else it is a true or false directly.
                    return Boolean.parseBoolean(conditionMatcher.group(1));
                }
            } else {
                // else it means that the condition is not valid.
                throw new SyntaxError("Invalid If Condition");
            }
        }
    }

    // class for running else statements.
    static class createElse {

        List<String> elseLines = new ArrayList<>(); // stores lines inside the else statement.
        Stack<String> elseStack = new Stack<>(); // stores the else statement lines for execution.
        Map<String, Object> elseVariableMap = new HashMap<>(); // keeps track of variable values during else execution.

        int indexElse; // index where the else statement starts in the currentLines code.
        int previousIfIndex; // index value of the previous if statement.

        createElse(int index, List<String> currentLines, Map<String, Object> currentVariableMap) throws AssignmentExceptions, SyntaxError {

            // Get index of a previous if statement.
            String[] spletCodeLine = currentLines.get(index).split(" ");
            for (int i = index + 1; !Objects.equals(spletCodeLine[0], "if"); i--){
                previousIfIndex = currentLines.indexOf(currentLines.get(i));
                spletCodeLine = currentLines.get(previousIfIndex).split(" ");
            }

            indexElse = index;
            elseVariableMap.putAll(currentVariableMap); // Copy the outer scope variables to the else statement scope.
            int i = index + 1; // get next line after else.
            // add inner else statement code to the elseLines.
            while (!Objects.equals(currentLines.get(i), "end")){
                elseLines.add(currentLines.get(i).substring(2));
                i++;
            }
            // add else statement lines to the stack for execution.
            for (int j = elseLines.size() - 1; j >= 0; j--) {
                String line = elseLines.get(j);
                if (Statement.addToStack(line)) {
                    elseStack.push(line.strip());
                }
            }
            // Execute the lines if previous if statement's condition is not met.
            if (!condition_is_met(currentLines)) {
                Stack<String> elseInnerStack = new Stack<>(); // new stack for the else statement lines.
                elseInnerStack.addAll(elseStack);
                runAlgorithm(elseLines, elseInnerStack, elseVariableMap); // run else statement code.
            }
        }

        // Checks if the given condition is met.
        public boolean condition_is_met(List<String> currentLines) throws SyntaxError{
            // parse previous if's condition part
            String whileLine = currentLines.get(previousIfIndex).strip();
            Matcher matcher = Statement.IF.getPattern().matcher(whileLine);
            String Condition = "";

            if (matcher.find()) {
                Condition = matcher.group(1);
            }else {
                // if IF didn't match throw an exception.
                throw new SyntaxError("Invalid If Statement");
            }
            // parse previous if's condition part

            // Matches the condition.
            Matcher conditionMatcher = Helper.CONDITION.getPattern().matcher(Condition);

            if (conditionMatcher.find()) {
                // check if the condition contains any of the relational operators.
                if (Helper.CONDITION.getPattern().toString().matches(".*[><=].*")){
                    // exctract from the condition.
                    String leftOperand = conditionMatcher.group(1);
                    String comparator = conditionMatcher.group(2);
                    String rightOperand = conditionMatcher.group(3);

                    // if operands are variables, get their value.
                    if (elseVariableMap.containsKey(leftOperand)){
                        leftOperand = elseVariableMap.get(leftOperand).toString();
                    }
                    if (elseVariableMap.containsKey(rightOperand)){
                        rightOperand = elseVariableMap.get(rightOperand).toString();
                    }
                    // compare the values depending on the comparator.
                    return switch (comparator) {
                        case "==" -> Long.parseLong(leftOperand) == Long.parseLong(rightOperand);
                        case "!=" -> Long.parseLong(leftOperand) != Long.parseLong(rightOperand);
                        case "<" -> Long.parseLong(leftOperand) < Long.parseLong(rightOperand);
                        case ">" -> Long.parseLong(leftOperand) > Long.parseLong(rightOperand);
                        case "<=" -> Long.parseLong(leftOperand) <= Long.parseLong(rightOperand);
                        case ">=" -> Long.parseLong(leftOperand) >= Long.parseLong(rightOperand);
                        default -> false;
                    };
                // if the variable has a boolean value
                } else if (elseVariableMap.containsKey(matcher.group(1))){
                     // if variable isn't boolean, returns false
                    return Boolean.parseBoolean(elseVariableMap.get(matcher.group(1)).toString()); // if variable isn't boolean, returns false
                } else {
                    // else it is a true or false directly.
                    return Boolean.parseBoolean(conditionMatcher.group(1));
                }
            } else {
                // else it means that the condition is not valid.
                throw new SyntaxError("Invalid If Condition");
            }
        }
    }
}

enum Statement {
    //defines different types of statements in a Ruby programming language, using regular expressions (regex) for pattern matching.
    
    ASSIGNMENT("([a-z_][a-zA-Z0-9_]*)\\s*(\\+=|-=|\\*=|\\/=|=|%=)\\s*([a-z_][a-zA-Z0-9_]*|\\d+|\\s*[\\+\\-\\*\\/%]?\\s*([a-z_][a-zA-Z0-9_]*|\\d+|\\([^\\)]+\\))\\s*)+\\s*$"),
    // ASSIGNMENT Group 1: Variable Name; Group 2: Operator; Group 3: Variable Value;
    IF("if \\s*(" + Helper.CONDITION.getPattern().toString() + ")\\s*$"),
    // IF Group 1: Condition;
    ELSE("else"),
    WHILE("while \\s*(" + Helper.CONDITION.getPattern().toString() + ")\\s*$"),
    // WHILE Group 1: Condition;
    FOR("for .*"),
    END("end"),
    PRINT("puts ([a-z_][a-zA-Z0-9_]*|\\d+)$"),
    // PRINT Group 1: item to print out;
    SCOPE("  .*"),
    EMPTY("");

    private final Pattern pattern; // holds the compiled regex.

    // takes regex string and complies it into a Pattern object.
    Statement(String regex) {
        this.pattern = Pattern.compile(regex);
    }
    // getter for pattern.
    public Pattern getPattern() {
        return pattern;
    }
    // checks if the code line matches the regex pattern.
    public boolean matches(String code) {
        return pattern.matcher(code).find();
    }
    // checks if a given code line is a valid statement to add to a stack for execution.
    public static boolean addToStack(String code_line) throws SyntaxError{
        if (getStatement(code_line) == ASSIGNMENT ||
                getStatement(code_line) == IF ||
                getStatement(code_line) == ELSE ||
                getStatement(code_line) == WHILE ||
                getStatement(code_line) == FOR ||
                getStatement(code_line) == PRINT ||
                getStatement(code_line) == END) {
            return true;
        }
        return false;
    }

    // checks which type of statement the given code line is.
    public static Statement getStatement(String code_line) throws SyntaxError{
        for (Statement s : values()) {
            if (Pattern.matches(s.pattern.toString(), code_line)) {
                return s;
            }
        }
        // if no match is found, then it's a syntax error or not compatible for our interpreter.
        throw new SyntaxError("Invalid Syntax (or too complex implementation for our interpreter) at: " + code_line);
    }
}

enum Helper {
    // provides additional regex patterns for easing the code validation process.
    
    CONDITION("([a-z_][a-zA-Z0-9_]*|\\d+)\\s*(==|!=|<=|>=|<|>)\\s*([a-z_][a-zA-Z0-9_]*|\\d+)$|true|false|[a-z_][a-zA-Z0-9_]*"),
    // CONDITION Group 1: left operand; Group 2: comparator; Group 3: right operand.
    ASSIGNMENT_HELPER("\\+=|-=|\\*=|\\/=|=|%=|=");

    private final Pattern pattern; // stores compiled regex.
    // constructor.
    Helper(String regex) {
        this.pattern = Pattern.compile(regex);
    }
    // getter for patter.
    public Pattern getPattern() {
        return pattern;
    }
}
