public class TerminalException extends Exception {
    private String message = "Generic Terminal Error";

    public TerminalException() {

    }

    public TerminalException(String msg) {
        message = msg;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
