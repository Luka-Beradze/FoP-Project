import java.util.regex.Pattern;
public class SumOfFirstN {
    public static void main(String[] args) {
        // The given first RuBY algorithm
        String RubyCode = """
                   n = 10
                   sum = 0
                   i = 1
                
                   while i <= n
                     sum += i
                     i += 1
                   end
                 """;

        // split each line of the code to check for matching
        String[] codeLines = RubyCode.split("\n");
        for (String line : codeLines) {
            Statements statement = Statements.getStatement(line.trim());  //remove whitespaces and call the method for matching the line
            if (statement != null) {
                System.out.println("Statement Type: " + statement);   // when identified, print the statement type
            } else {
                System.out.println("No match for line: " + line);
            }
        }
    }

    // create enum to check and identify each code line
    enum Statements{
        Assignment("\\w+\\s*(=|\\+=|-=)\\s*\\w+"), // handles assignments
        While("while +(\\w+|\\d+) *(<=|<|>=|>|==|!=) *(\\w+|\\d+)"),   // handles while loop
        End("end"),    // handles end statements
        Blank(" *");   // handles empty lines or whitespaces

        // Constructor to compile the regex for each statement type
        private final Pattern pattern;
        Statements(String regex){
            this.pattern = Pattern.compile(regex);
        }

        // Method to check if each line matches the current statement type
        public boolean matches(String code) {
            return pattern.matcher(code).find();
        }

        // Method to identify the statement type for a given code line
        public static Statements getStatement(String code) {
            for (Statements statement : values()) {
                if (statement.matches(code)) {
                    return statement;
                }
            }
            return null; // If no statement type matches
        }
    }
}
