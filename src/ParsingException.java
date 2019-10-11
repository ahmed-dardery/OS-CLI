public class ParsingException extends Exception {
    //TODO : implement exception class
    String message = "Parsing Error";
    public ParsingException(){}
    public ParsingException(String msg){
        message = msg;
    }
    public String getMessage(){
        return message;
    }
}
