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
    public static String stopApplicationMessage = "HALT";
    public static String homeDir = System.getProperty("user.dir");
    public static String workingDir = homeDir;

    public static void main(String[] args) {
        Terminal kernel = new Terminal(System.in, System.out);

        String input;
        do {
            System.out.print(ConsoleColor.Colorify(ConsoleColor.ANSI.GREEN, System.getProperty("user.name")));
            System.out.print(":");
            System.out.print(ConsoleColor.Colorify(ConsoleColor.ANSI.BLUE, workingDir.replace(homeDir, "~")));
            System.out.print("$ ");

            input = in.nextLine();
            try {
                String[] res = input.split("\\|");
                String given = null;
                for (int i = 0; i < res.length; ++i) {
                    String entry = res[i];
                    Parser p = new Parser(entry, given);
                    String ret = kernel.exec(p);
                    if (p.getRedirectionType() == Parser.RedirectionType.NoRedirection) {
                        if (i != res.length - 1) {
                            given = ret;
                        } else if (p.getCmd().equals("ls")) {
                            //replace ":something" with "something" colored blue
                            ret = ret.replaceAll(":( [^\n]*(\n|$))", ConsoleColor.Colorify(ConsoleColor.ANSI.BLUE, "$1"));
                        }
                        System.out.println(ret);
                    } else {
                        boolean append = p.getRedirectionType() == Parser.RedirectionType.Append;
                        try {
                            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(p.getRedirectionFilename(), append), StandardCharsets.UTF_8);
                            if (append)
                                out.write('\n');
                            out.write(ret);
                            out.flush();
                            out.close();
                        } catch (Exception ex) {
                            System.out.println(ConsoleColor.Colorify(ConsoleColor.ANSI.RED, "redirection: unable to redirect output to file."));
                        }
                    }
                }
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().equals(stopApplicationMessage)) break;
                System.out.println(ConsoleColor.Colorify(ConsoleColor.ANSI.RED, e.getMessage()));
                //e.printStackTrace();
            }

        } while (true);
    }
}