public class ParsingException extends Exception {
    private String message = "Generic Parsing Error";

    public ParsingException() {

    }

    public ParsingException(String msg) {
        message = msg;
    }

    public String getMessage() {
        return message;
    }
}
