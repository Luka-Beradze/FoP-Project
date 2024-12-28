package Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
