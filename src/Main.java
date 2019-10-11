import java.util.Scanner;

public class Main {
    //clear, cd, ls, cp, mv, rm, mkdir, rmdir, cat, more, pwd.

    private static Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Currently in development phase...");
        String input;
        do{
            input = in.nextLine();
            //Parser test();
        }while(!input .equals("exit"));
    }
}
