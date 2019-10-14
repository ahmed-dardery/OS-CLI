public class ConsoleColor {
    enum ANSI {
        BLACK(30), RED(31), GREEN(32), YELLOW(33), BLUE(34), PURPLE(35), CYAN(36), WHITE(37);

        private int value;

        ANSI(int v) {
            value = v;
        }

        int getANSIValue() {
            return value;
        }
    }

    public static String Colorify(ANSI color, String msg) {
        return String.format("%s%d%c%s%s", "\u001B[", color.getANSIValue(), 'm', msg, "\u001B[0m");
    }
}
