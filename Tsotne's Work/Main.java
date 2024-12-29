package Learning;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Interpreter {

    private static final String algorithm = "src/Algorithms/SumOfN.txt"; // Points to a file ** containing the Ruby algorithm to be interpreted.

    public static Path path = Path.of(algorithm); // creates a Path object pointing to "src/Algorithms/SumOfN.txt".
    public static List<String> lines; //This file is read and stored in a list called "lines".
    static {
        try {
            lines = Files.readAllLines(path); //reads the file's content into a List<String>.
        } catch (IOException e) {
            throw new RuntimeException(e); //If pointed file does not exist or cannot be opened, this block will trigger and crash the program, showing why the error occurred.
        }
    }

    public static Stack<String> runningStack = new Stack<>(); // A stack that stores code lines to be executed in reverse order
    public static Map<String, Object> variableMap = new HashMap<>(); // Stores variable names and their values for the Ruby code.



    public static void main(String[] args){


        for (int i = lines.size() - 1; i >= 0; i--) { // Iterates in reverse through the lines list.
            String line = lines.get(i);  //This line retrieves the current line of code from the lines list at index i.


            if (Statement.addToStack(line)) { // Checks if a line is a valid statement (e.g., assignment, loop, print).
                runningStack.push(line.strip()); // Adds valid lines to runningStack after stripping whitespace.
            }
        }
        System.out.println(runningStack);

        runAlgorithm(lines, runningStack, variableMap); // run's algorithm line by line.
    }

    private static void runAlgorithm(List<String> currentLines, Stack<String> currentRunningStack, Map<String, Object> currentVariableMap) {

        while (!currentRunningStack.empty()){     //The loop iterates as long as there are items in currentRunningStack.
            switch (Statement.getStatement(currentRunningStack.getLast())){  //Determines the type of statement for the topmost line in the stack.
                case null:  //If the line does not match any statement type, it does nothing and breaks the switch.
                    break;
                case ASSIGNMENT:
                    parseKeyValuePair(currentRunningStack.pop(), currentVariableMap); //stores the variable-value pair in currentVariableMap.
                    break;
                case IF:
                    currentRunningStack.pop(); //es gasaketebelia
                    break;
                case ELSE:
                    currentRunningStack.pop(); //es gasaketebelia
                    break;
                case WHILE:
                    int indexWhile = currentLines.indexOf(currentRunningStack.getLast()); //Finds the index of the while statement in currentLines.

                    createWhileLoop whileLoop = new createWhileLoop(indexWhile, currentLines, currentVariableMap); //Extracts the loop body.

                    // update variables
                    whileLoop.whileVariableMap.forEach((key, value) -> {  // Iterates over each key-value pair in whileVariableMap.
                        if (currentVariableMap.containsKey(key)) {  // Checks if the variable (key) exists in the main currentVariableMap before updating it.

                            currentVariableMap.put(key, value);   // Updates the value of the variable in currentVariableMap to match the value in whileVariableMap.
                        }
                    });

                    currentRunningStack.pop();  // removes top element.
                    break;
                case FOR:
                    currentRunningStack.pop();  //es gasaketebelia
                    break;
                case END:
                    currentRunningStack.pop();  // Poos the end keyword, marking the end of a block.
                    break;
                case PRINT:
                    int indexPrint = currentLines.indexOf(currentRunningStack.getLast());  // Finds the index of the print statement in currentLines.

                    runPrint(indexPrint, currentLines, currentVariableMap);  // displays the value of the variable or literal in the print statement.

                    currentRunningStack.pop();  // Pops the print statement from the stack.
                    break;
              // Pops lines that represent indentation or empty lines, effectively skipping them.
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
        Matcher matcher = Statement.ASSIGNMENT.getPattern().matcher(code_line);  //  initializes a Matcher object to apply a regular expression (regex) pattern to the input line of code (code_line).
        if (matcher.find()) {  // checks whether the input code_line matches the assignment pattern.
            String key = matcher.group(1);  // The variable being assigned (e.g., x).
            String operator = matcher.group(2);  // The operator (e.g., =, +=, -=).
            Object value; // Object that will store the value being assigned to the variable.

            // Assign value
            try {
                value = Long.valueOf(matcher.group(3)); //converts value into a Long.
            } catch (NumberFormatException e) { //throws exception if it cannot be converted to a Long.
                // if variable, assign its value
                if (currentVariableMap.containsKey(matcher.group(3))) {
                    value = currentVariableMap.get(matcher.group(3));  // If the variable exists in the map, it assigns its value to value.
                } else {
                    // else boolean; Only false or true reaches this point.
                    value = Boolean.valueOf(matcher.group(3));
                }
            }

            // Handle compound assignment operators
         //checks if the variable (represented by key) exists in currentVariableMap and if the value being assigned is of type Long.
            if (currentVariableMap.containsKey(key) && value instanceof Long) {
                Long currentValue = (Long) currentVariableMap.get(key);
                Long numericValue = (Long) value;

                switch (operator) {
                    case "+=":
                        currentVariableMap.put(key, currentValue + numericValue);  // Adds numericValue to the current value of the variable and updates it.
                        break;
                    case "-=":
                        currentVariableMap.put(key, currentValue - numericValue);  //  Subtracts numericValue from the current value of the variable and updates it.
                        break;
                    case "*=":
                        currentVariableMap.put(key, currentValue * numericValue);  // Multiplies the current value of the variable by numericValue and updates it.
                        break;
                    case "/=":
                        if (numericValue != 0) {
                            currentVariableMap.put(key, currentValue / numericValue);  // Divides the current value of the variable by numericValue and updates it (of course when numereticValue is not zero).
                        } else {
                            throw new ArithmeticException("Division by zero"); // Exception is thrown to prevent division by zero.
                        }
                        break;
                    case "%=":
                        if (numericValue != 0) {
                            currentVariableMap.put(key, currentValue % numericValue);  // Performs modulo (remainder of division) on the current value and numericValue
                        } else {
                            throw new ArithmeticException("Modulo by zero is undefined");  // throws an exception if numereticValue is zero.
                        }
                        break;
                    default: // Simple assignment
                        currentVariableMap.put(key, value); // It just assigns the value directly to the key in currentVariableMap.
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

    public static void runPrint(int index, List<String> currentLines, Map<String, Object> currentVariableMap) {
        String printLine = currentLines.get(index).strip();  // Strip removes leading whitespace.
        Matcher matcher = Statement.PRINT.getPattern().matcher(printLine);  // Trys to match the print statement pattern.
        String print = "";  // Variable to hold what should be printed.

        if (matcher.find()) {  // If a match is found.
            if (currentVariableMap.containsKey(matcher.group(1))){  // Check if the value is a variable.
                print = currentVariableMap.get(matcher.group(1)).toString();  // If it's a variable, get its value.
            } else {
                print = matcher.group(1);  // If it's a literal (e.g., a string or number), use it directly.
            }
            System.out.println(print);  //pretty self-explanatory.
        } else {
            System.out.println("Invalid Print Statement");  // If the print statement is malformed, print an error message (might become exception)
        }
    }

    static class createWhileLoop {

        List<String> whileLines = new ArrayList<>();  // Stores lines inside the while loop
        Stack<String> whileStack = new Stack<>();  // Stores the loop's statements for execution
        Map<String, Object> whileVariableMap = new HashMap<>();  // Keeps track of variable values during loop execution

        int indexWhile;  // The index where the while loop starts in the current code

        createWhileLoop(int index, List<String> currentLines, Map<String, Object> currentVariableMap){
            indexWhile = index;  // Set the starting index of the while loop
            whileVariableMap.putAll(currentVariableMap);  // Copy the current variables to the while loop context
         // Gathering lines that belong to the while loop (of course excluding the "end")
            int i = index + 1; // get next line after while
            while (!Objects.equals(currentLines.get(i), "end")){  // Continue until we hit the "end" line
                whileLines.add(currentLines.get(i).substring(2));  // Add the line (without the indentation)
                i++;  // Move to the next line
            }
          // Adding the loop statements to the stack
            for (int j = whileLines.size() - 1; j >= 0; j--) {  // Iterate backwards through the loop lines
                String line = whileLines.get(j);
                if (Statement.addToStack(line)) {  // If the statement can be added to the stack (based on its type)
                    whileStack.push(line.strip());  // Push the stripped line onto the stack
                }
            }
         // Executing the loop as long as the condition is met
            while (condition_is_met(currentLines)){
                Stack<String> loopStack = new Stack<>();  // Create a new stack for the current loop iteration
                loopStack.addAll(whileStack);  // Add all the statements from the while loop's stack
                runAlgorithm(whileLines, loopStack, whileVariableMap);  // runs the loop with given arguments
            }
        }

        public boolean condition_is_met(List<String> currentLines){
            // parse condition part
            String whileLine = currentLines.get(indexWhile).strip();  // Get the line of the while loop
            Matcher matcher = Statement.WHILE.getPattern().matcher(whileLine);  // Create a matcher to find the pattern defined for the "while" loop
            String Condition = "";
         
            // If the pattern matches, extract the condition part of the while loop
            if (matcher.find()) {
                Condition = matcher.group(1);  // Extract the condition from the while statement
            }else {
                System.out.println("Invalid While Line");
                return false;  // Return false if the while loop line doesn't match the expected format
            }
            // parse condition part

            Matcher conditionMatcher = Helper.CONDITION.getPattern().matcher(Condition);  // Matcher object checks if the condition string (Condition) matches the pattern defined in Helper.CONDITION.

            if (conditionMatcher.find()) {   //checks whether the pattern for the condition was found in the line.
                if (Helper.CONDITION.getPattern().toString().matches(".*[><=].*")){  //the condition is checked to see if it contains any of the relational operators
                 //Extraction
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
                   //Evaluation of the condition based on the comparator, conversion of both operands to Long and comparasion between them using specified operator
                    return switch (comparator) {
                        case "==" -> Long.parseLong(leftOperand) == Long.parseLong(rightOperand);
                        case "!=" -> Long.parseLong(leftOperand) != Long.parseLong(rightOperand);
                        case "<" -> Long.parseLong(leftOperand) < Long.parseLong(rightOperand);
                        case ">" -> Long.parseLong(leftOperand) > Long.parseLong(rightOperand);
                        case "<=" -> Long.parseLong(leftOperand) <= Long.parseLong(rightOperand);
                        case ">=" -> Long.parseLong(leftOperand) >= Long.parseLong(rightOperand);
                        default -> false;
                    };
                // checks if the variable has a boolean value and returns it
                } else if (whileVariableMap.containsKey(matcher.group(1))){
                    return Boolean.parseBoolean(whileVariableMap.get(matcher.group(1)).toString()); // if variable isn't boolean, returns false
                } else {
                    return Boolean.parseBoolean(conditionMatcher.group(1)); //If the value is not a variable but a literal boolean (true or false), it parses it directly.
                }
            } else {
                System.out.println("Invalid Condition"); // also might become exception
            }
            return false;
        }

    }

}

enum Statement {
//defines different types of statements in a programming language, using regular expressions for pattern matching.
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

    private final Pattern pattern;  //declares a private instance variable pattern that holds the compiled regular expression for each statement in the enum.

    Statement(String regex) {
        this.pattern = Pattern.compile(regex);  // takes a regex string as input and compiles it into a Pattern object.
    }

    public Pattern getPattern() {  // returns the Pattern object associated with the current enum constant.
        return pattern;  
    }

    public boolean matches(String code) {  //This method checks whether a given code line matches the regex pattern associated with the current enum constant.
        return pattern.matcher(code).find();
    }

    public static boolean addToStack(String code_line) {  //checks if a given code line corresponds to a valid statement that should be added to a stack for execution.
        if (getStatement(code_line) == ASSIGNMENT ||
                getStatement(code_line) == IF ||
                getStatement(code_line) == WHILE ||
                getStatement(code_line) == FOR ||
                getStatement(code_line) == PRINT ||
                getStatement(code_line) == END) {
            return true;
         //essentially filtering out any code lines that don't correspond to a statement that needs further processing.
        }
        return false;
    }

//This method looks at the given line of code and checks which type of statement it matches based on predefined patterns.
    public static Statement getStatement(String code_line) {
        for (Statement s : values()) {  //It goes through all the possible statement types
            if (Pattern.matches(s.pattern.toString(), code_line)) {  //If a match is found, it returns that statement type
                return s;
            }
        }
        return null;  //If no match is found, it returns null.
    }
}

enum Helper {

    CONDITION("([a-z_][a-zA-Z0-9_]*|\\d+)\\s*(==|!=|<=|>=|<|>)\\s*([a-z_][a-zA-Z0-9_]*|\\d+)$|true|false|[a-z_][a-zA-Z0-9_]*"),// Does not contain "!" before a condition
    // CONDITION Group 1: left operand; Group 2: comparator; Group 3: right operand.
    ASSIGNMENT_HELPER("\\+=|-=|\\*=|\\/=|=|%=|=");

 //declares a field named pattern in the Helper enum.
    private final Pattern pattern;

    Helper(String regex) {  //constructor of the Helper enum
        this.pattern = Pattern.compile(regex);  //compiles the provided regex string into a Pattern object.
    }

    public Pattern getPattern() { // returns the compiled pattern for use elsewhere in the code.
        return pattern;
    }
}
