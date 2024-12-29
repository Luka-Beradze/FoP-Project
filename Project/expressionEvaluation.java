package Project;

import java.util.Stack;

public class expressionEvaluation {
    public static Long evaluate(String expression) throws assignmentException {
        char[] tokens = expression.toCharArray();

        Stack<Integer> values = new Stack<Integer>();
        Stack<Character> ops = new Stack<Character>();

        for (int i = 0; i < tokens.length; i++) {
            // Check for undefined variables
            if ((tokens[i] >= 'A' && tokens[i] <= 'Z') || (tokens[i] >= 'a' && tokens[i] <= 'z')) {
                throw new assignmentException("Variable in a variable assignment expression {" + expression + "} is not defined!");
            }

            // Skip spaces
            if (tokens[i] == ' ') {
                continue;
            }

            // If the token is a digit, process it
            if (tokens[i] >= '0' && tokens[i] <= '9') {
                StringBuffer sbuf = new StringBuffer();
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
            // Handle operators (+, -, *, /)
            else if (tokens[i] == '+' || tokens[i] == '-' ||
                    tokens[i] == '*' || tokens[i] == '/') {
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

        return (long) values.pop();
    }

    public static boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') {
            return false;
        }
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) {
            return false;
        }
        return true;
    }

    public static int applyOp(char op, int b, int a) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) {
                    throw new UnsupportedOperationException("Cannot divide by zero");
                }
                return a / b;
        }
        return 0;
    }
}
