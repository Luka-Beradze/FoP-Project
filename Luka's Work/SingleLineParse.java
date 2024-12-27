import java.util.*;

public class singleLineParse {
    public enum TokenType {
        //types of tokens (elements of code)
        NUMBER, IDENTIFIER, KEYWORD, OPERATOR, DELIMITER, WHITESPACE, EOF
    
    }

    public static class Token {
        //defining token
        public final TokenType type;
        public final String value;

        public Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }

        public String toString() {
            return "Token{" + "type=" + type + ", value='" + value + '\'' + '}';
        }
    }

    private final String input;
    private int position;
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList("if", "else", "while", "end"));

    //parse single line
    public singleLineParse(String input) {
        this.input = input; //input
        this.position = 0;  //character position when reading line
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (position < input.length()) {
            char current = input.charAt(position);

            //find token type
            //whitespace
            if (Character.isWhitespace(current)) {
                position++;
                continue;
            }

            //digits
            if (Character.isDigit(current)) {
                tokens.add(new Token(TokenType.NUMBER, readNumber()));
                continue;
            }


            //Strings
            if (Character.isLetter(current)) {
                String identifier = readIdentifier();
                TokenType type = KEYWORDS.contains(identifier) ? TokenType.KEYWORD : TokenType.IDENTIFIER;
                tokens.add(new Token(type, identifier));
                continue;
            }

            //operators
            if ("+-*/%()=<>".indexOf(current) != -1) {
                tokens.add(new Token(TokenType.OPERATOR, String.valueOf(current)));
                position++;
                continue;
            }

            throw new RuntimeException("Unexpected character: " + current);
        }

        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }

    private String readNumber() {
        StringBuilder sb = new StringBuilder();
        while (position < input.length() && Character.isDigit(input.charAt(position))) {
            sb.append(input.charAt(position++));
        }
        return sb.toString();
    }

    private String readIdentifier() {
        StringBuilder sb = new StringBuilder();
        while (position < input.length() && Character.isLetterOrDigit(input.charAt(position))) {
            sb.append(input.charAt(position++));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String code = "if x = 5 then y = x + 1 end"; //single line of code
        singleLineParse lexer = new singleLineParse(code);
        List<Token> tokens = lexer.tokenize();
        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}
