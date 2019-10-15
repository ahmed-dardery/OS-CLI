//TODO: Modify error messages to be as linux
//TODO: Modify console output to be like linux
//TODO: use ConsoleColor to output colorful messages when appropriate (see catch block below)
//TODO: finish rest of TODOs

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    private static Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        Terminal kernel = new Terminal(System.in, System.out);

        System.out.println("Currently in development phase...");
        System.out.println("\033[H\033[2J");

        String input;
        do {
            System.out.print(kernel.getWorkingDirectory());
            System.out.print(" : ");

            input = in.nextLine();
            if (input.equals("clear")) {
                // TODO find a better way for clear
                for (int i = 0 ; i<100 ;++i) System.out.println();
                continue;
            }
            else if (input.equals("exit")) break;
            try {
                Parser[] parsers = Parser.parseUserInput(input);
                for (Parser p : parsers) {
                    String ret = kernel.exec(p);
                    if (p.getRedirectionType() == Parser.RedirectionType.NoRedirection) {
                        if (p.getCmd().equals("ls")) {
                            ret = ret.replaceAll(":( [^\n]*(\n|$))", ConsoleColor.Colorify(ConsoleColor.ANSI.BLUE, "$1"));
                        }
                        System.out.println(ret);
                    } else {
                        boolean append = p.getRedirectionType() == Parser.RedirectionType.Append;
                        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(p.getRedirectionFilename(), append), StandardCharsets.UTF_8);
                        if (append)
                            out.write('\n');
                        out.write(ret);
                        out.flush();
                        out.close();
                    }
                }
            } catch (Exception e) {
                if (e.getMessage() == "HALT") break;
                System.out.println(ConsoleColor.Colorify(ConsoleColor.ANSI.RED, e.getMessage()));
                e.printStackTrace();
            }

        } while (!input.equals("exit"));
    }
}
