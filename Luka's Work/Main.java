import java.util.List;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Main { 
    public static void main(String[] args) {
        String input = "n = 10\n" + 
                        "sum = 0\n" + 
                        "i = 1\n" + 
                        "\n" + 
                        "while i <= n\n" + 
                        "  sum += i\n" + 
                        "  i += 1\n" + 
                        "end";
        ArrayList<String> lines;
        
        for (String i : ) {
            
        }

        lines.forEach(l -> {
            System.out.println(Statement.getStatement(l));
        });
    }
}

enum Statement {
    
    ASSIGNMENT("(\\w+ *=\\w+)"),
    IF("if +(\\w+|\\d+) *(< |> |== |<= |>= |!= ) *(\\w+|\\d+)"),
    ELSE("else"),
    WHILE("while +(\\w+|\\d+) *(< |> |== |<= |>= |!= ) *(\\w+|\\d+)"),
    CONDITION(".*(==|!=|<=|>=|<|>).*"),
    PRINT("puts +(\\w+|\\d+)"),
    SPACE(" +"),
    EOF("");

    private final Pattern pattern;

    Statement(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public boolean matches(String code) {
        return pattern.matcher(code).find();
    }

    public static Statement getStatement(String code) {
        for (Statement s : values()) {
            if (s.matches(code)) {
                return s;
            }
        }
        return null;
    }

}
