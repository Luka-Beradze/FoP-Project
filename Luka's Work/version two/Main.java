import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Simulated Ruby input
        /*
        * the code format is as follows:
        * variable = value
        * at least one space between equity
        *
        * ```while condition
        *    code
        *    end
        * ```
        * same goes for while loop
        *
        *
        * */
        String rubyCode = """
            
            
            puts 00000

            k = 8
            k %= 5
            puts k

            puts 00001
            
            i = 1
            n = 10
            sum = 0
            
            if i < n
             puts 1
            else
             puts 0
            end
          
            while i <= n
                sum += i
                i += 1
            end
            
            puts sum
            
            
            n = 5
            sum = 1
            i = 1
        
            while i <= n
              sum *= i
              i += 1
            end
            
            puts sum
            
            
            a = 48
            b = 18
        
            while b > 0
              temp = b
              c = a
              c %= b
              b = c
              a = temp
            end
        
            puts a
            
            puts 00002
            
            num = 1234
            reversed = 0
        
            while num != 0
              p = num
              p %= 10
              digit = p
              reversed *= 10
              reversed += digit
              num /= 10
            end
        
            puts reversed
        """;

        interpretRuby(rubyCode);
    }

    public static void interpretRuby(String code) {
        // Store variables in a map (simulates Ruby's variable environment)
        Map<String, Integer> variables = new HashMap<>();

        // Split code into lines and process sequentially
        String[] lines = code.split("\n");
        int i = 0;

        while (i < lines.length) {
            //remove surrounding spaces
            String line = lines[i].trim();

            if (line.startsWith("while")) {
                // Parse the while condition
                String condition = line.substring(6).trim(); // Skip "while "
                int startLine = i;

                // Find the corresponding "end" line
                int endLine = findEnd(lines, startLine);

                // Execute the while loop
                while (evaluateCondition(condition, variables)) {
                    for (int j = startLine + 1; j < endLine; j++) {
                        executeLine(lines[j], variables);
                    }
                }

                // Skip to the end of the loop
                i = endLine;
            } else if (line.startsWith("if")) {
                // Parse the if condition
                String condition = line.substring(3).trim(); // Skip "if "
                int startLine = i;

                // Find the corresponding `else` and `end`
                int elseLine = findElse(lines, startLine);
                int endLine = findEnd(lines, startLine);

                // Evaluate the if condition
                if (evaluateCondition(condition, variables)) {
                    // Execute the block inside the if statement
                    for (int j = startLine + 1; j < elseLine; j++) {
                        executeLine(lines[j], variables);
                    }
                } else {
                    // Execute the block inside the else statement
                    for (int j = elseLine + 1; j < endLine; j++) {
                        executeLine(lines[j], variables);
                    }
                }

                // Skip to the end of the if-else block
                i = endLine;
            } else {
                // Execute a single line
                executeLine(line, variables);
            }


            i++;
        }
    }

    private static int findElse(String[] lines, int start) {
        for (int i = start + 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.equals("else")) {
                return i;
            } else if (line.equals("end")) {
                return i; // No else block
            }
        }
        throw new RuntimeException("Syntax Error: 'else' or 'end' not found.");
    }

    static int findEnd(String[] lines, int start) {
        for (int i = start + 1; i < lines.length; i++) {
            if (lines[i].trim().equals("end")) {
                return i;
            }
        }
        throw new RuntimeException("Syntax Error: 'end' not found.");
    }

    static boolean evaluateCondition(String condition, Map<String, Integer> variables) {
        // Simple condition parsing: `i <= n`
        String[] parts = condition.split(" ");
        int left = evaluateExpression(parts[0], variables);
        String operator = parts[1];
        int right = evaluateExpression(parts[2], variables);

        return switch (operator) {
            case "<=" -> left <= right;
            case "<" -> left < right;
            case "==" -> left == right;
            case "!=" -> left != right;
            case ">" -> left > right;
            case ">=" -> left >= right;
            default -> throw new RuntimeException("Unknown operator: " + operator);
        };
    }

    static void executeLine(String line, Map<String, Integer> variables) {
        if (line.startsWith("puts")) {
            // Handle printing
            String expr = line.substring(5).trim(); // Skip "puts "
            int value = evaluateExpression(expr, variables);
            if (variables.containsKey(expr)) {
                System.out.println(variables.get(expr)); //print expression
            } else {
                System.out.println(expr.replace("\"", "")); // Print or Number
            }
        } else if (line.contains("+=")) {
            // Parse addition
            String[] parts = line.split("\\+=");
            String var = parts[0].trim();
            int value = evaluateExpression(parts[1].trim(), variables);
            variables.put(var, variables.getOrDefault(var, 0) + value);
        } else if (line.contains("-=")) {
            // Parse subtraction
            String[] parts = line.split("-=");
            String var = parts[0].trim();
            int value = evaluateExpression(parts[1].trim(), variables);
            variables.put(var, variables.getOrDefault(var, 0) - value);
        } else if (line.contains("*=")) {
            // Parse multiplication
            String[] parts = line.split("\\*=");
            String var = parts[0].trim();
            int value = evaluateExpression(parts[1].trim(), variables);
            variables.put(var, variables.getOrDefault(var, 0) * value);
        } else if (line.contains("/=")) {
            // Parse division
            String[] parts = line.split("/=");
            String var = parts[0].trim();
            int value = evaluateExpression(parts[1].trim(), variables);
            if (value == 0) throw new RuntimeException("Division by zero");
            variables.put(var, variables.getOrDefault(var, 0) / value);
        }else if (line.contains("%=")) {
            // Parse division
            String[] parts = line.split("%=");
            String var = parts[0].trim();
            int value = evaluateExpression(parts[1].trim(), variables);
            if (value == 0) throw new RuntimeException("Division by zero");
            variables.put(var, variables.getOrDefault(var, 0) % value);
        } else if (line.contains("=")) {
            // split assignment line into var name and value
            String[] parts = line.split("=");
            String var = parts[0].trim();
            int value = evaluateExpression(parts[1].trim(), variables);
            variables.put(var, value);
        }
    }

    static int evaluateExpression(String expr, Map<String, Integer> variables) {
        // Handle integers and variables
        if (variables.containsKey(expr)) {
            return variables.get(expr);
        } else if (expr.matches("\\d+")) { // Check if it's a number
            return Integer.parseInt(expr);
        } else {
//            throw new RuntimeException("Unknown expression: " + expr);
            System.out.println("q");
        }
       throw new RuntimeException("Unknown expression: " + expr);
    }
}
