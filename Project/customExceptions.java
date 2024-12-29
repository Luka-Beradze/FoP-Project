package Project;

// define an assignment exception
class AssignmentExceptions extends Exception{

    public AssignmentExceptions(String message) {
        super(message);
    }
}

// define Syntax error
class SyntaxError extends Exception{
    public SyntaxError(String message) {
        super(message);
    }
}
