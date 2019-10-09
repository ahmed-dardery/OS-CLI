import java.util.Scanner;

public class Main {
    //clear, cd, ls, cp, mv, rm, mkdir, rmdir, cat, more, pwd.

    private static Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("test");
        System.out.println("test");
        System.out.println("test");
        System.out.println("test");
        System.out.println("test");
        in.next();
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.println("hi");
    }
}
