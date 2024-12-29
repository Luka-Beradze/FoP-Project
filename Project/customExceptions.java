package Project;

// define an assignment exception
class CustomExceptions extends Exception{

    public CustomExceptions(String message) {
        super(message);
    }
}

// define Syntax error
class SyntaxError extends Exception{
    public SyntaxError(String message) {
        super(message);
    }
}
