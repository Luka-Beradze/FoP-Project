package Project;

import java.util.Stack;

public class expressionEvaluation {
    public static Long evaluate(String expression) throws AssignmentExceptions {
        char[] tokens = expression.toCharArray();

        Stack<Integer> values = new Stack<>();
        Stack<Character> ops = new Stack<>();

        for (int i = 0; i < tokens.length; i++) {
            // Check for undefined variables
            if ((tokens[i] >= 'A' && tokens[i] <= 'Z') || (tokens[i] >= 'a' && tokens[i] <= 'z')) {
                throw new AssignmentExceptions("Variable in a variable assignment expression {" + expression + "} is not defined!");
            }

            // Skip spaces
            if (tokens[i] == ' ') {
                continue;
            }

            // If the token is a digit, process it
            if (tokens[i] >= '0' && tokens[i] <= '9') {
                StringBuilder sbuf = new StringBuilder();
                // There may be more than one digit in the number
                while (i < tokens.length && tokens[i] >= '0' && tokens[i] <= '9') {
                    sbuf.append(tokens[i]);
                    i++;
                }
                values.push(Integer.parseInt(sbuf.toString()));
                i--;
            }
            // Handle opening parenthesis
            else if (tokens[i] == '(') {
                ops.push(tokens[i]);
            }
            // Handle closing parenthesis
            else if (tokens[i] == ')') {
                while (ops.peek() != '(') {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.pop();
            }
            // Handle operators (+, -, *, /, %)
            else if (tokens[i] == '+' || tokens[i] == '-' ||
                    tokens[i] == '*' || tokens[i] == '/' || tokens[i] == '%') {
                while (!ops.empty() && hasPrecedence(tokens[i], ops.peek())) {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.push(tokens[i]);
            }
        }

        // Process any remaining operations
        while (!ops.empty()) {
            values.push(applyOp(ops.pop(), values.pop(), values.pop()));
        }
        // Return the evaluated value.
        return (long) values.pop();
    }

    public static boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') {
            return false;
        }
        return (op1 != '*' && op1 != '/' && op1 != '%') || (op2 != '+' && op2 != '-');
    }

    public static int applyOp(char op, int b, int a) {
        return switch (op) {
            case '+' -> a + b;
            case '-' -> a - b;
            case '*' -> a * b;
            case '/' -> {
                if (b == 0) {
                    throw new ArithmeticException("Cannot divide by zero");
                }
                yield a / b;
            }
            case '%' -> {
                if (b == 0) {
                    throw new ArithmeticException("Cannot divide by zero");
                }
                yield a % b;
            }
            default -> 0;
        };
    }
}
