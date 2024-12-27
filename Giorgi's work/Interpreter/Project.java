package Interpreter;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Project {

    private static final String algorithm = "D:\\IntelliJ\\File Directory\\FOP-Project\\src\\Interpreter\\Algorith.txt";

    public static void main(String[] args) throws IOException {
//        runAlgorithm(algorithm);
        Path path = Path.of(algorithm);
        List<String> lines = Files.readAllLines(path);

        lines.forEach(l -> {
            System.out.println(Statement.getStatement(l));
        });
    }

//    private static void runAlgorithm(String algorithm_path) throws IOException {
//        Path path = Path.of(algorithm_path);
//        Files.readAllLines(path).forEach(System.out::println);
//    }
}

enum Statement {

    ASSIGNMENT("([a-z]|_|$|@+)(\\w|\\d)*\\s*=\\s*\\d*|true|false$"),
    IF("if .*"),
    ELSE("else"),
    WHILE("while .*"),
    FOR("for .*"),
    END("end"),
    PRINT("puts .*$"),
    SCOPE("  "),
    EMPTY("");

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
