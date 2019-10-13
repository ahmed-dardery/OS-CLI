import java.util.Scanner;

public class Main {
    //clear, cd, ls, cp, mv, rm, mkdir, rmdir, cat, more, pwd.

    private static Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        Terminal kernal = new Terminal();

        System.out.println("Currently in development phase...");
        String input;
        do {
            System.out.print(kernal.getWorkingDirectory());
            System.out.print(" : ");

            input = in.nextLine();
            Parser parser = new Parser();
            try {
                parser.tryParse(input);
                kernal.exec(parser.getCmd(), parser.getArguments());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } while (!input.equals("exit"));
    }
}
