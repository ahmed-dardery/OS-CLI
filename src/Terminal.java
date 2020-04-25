import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;


class Terminal {

    private Scanner in;
    private PrintStream out;
    private Map<String, String> commandsArgs = new HashMap<>();

    public Terminal(InputStream input, PrintStream output) {
        in = new Scanner(input);
        out = output;

        commandsArgs.put("cat", "arg1: file arg2.n: files\nReads from and concatenate all files specified in the arguments.");
        commandsArgs.put("cd", "[arg1: new_dir]\nChanges current working directory to new_dir, if omitted changes it to Home Directory.");
        commandsArgs.put("mkdir", "arg1 : dir\nCreates an empty directory in path dir.");
        commandsArgs.put("rmdir", "arg1 : dir\nRemoves directory at path dir, directory should be empty.");
        commandsArgs.put("more", "arg1: file_path\nDisplays some of data in file_path, supports scrolling by Enter: next line, Space: one page, b: back one page.");
        commandsArgs.put("date", "no arguments\nPrints current system date and time.");
        commandsArgs.put("args", "[arg1: argument]\nDisplays arguments of the command specified in arg1.");
        commandsArgs.put("help", "[arg1: argument]\nDisplays info about arg1, if omitted displays info about all commands.");
        commandsArgs.put("cp", "arg1: file/dir arg2: file/dir\nCopies file/directory from arg1 to arg2.");
        commandsArgs.put("mv", "arg1: file/dir arg2: file/dir\nMoves file/directory from arg1 to arg2.");
        commandsArgs.put("rm", "arg1: file/Empty dir\nRemoves file/directory permanently.");
        commandsArgs.put("pwd", "no arguments\nDisplays the current working directory.");
        commandsArgs.put("ls", "[arg1: Directory]\nDisplays the files and subfolders in a directory , default: current working directory.");
        commandsArgs.put("clear", "no arguments\nClears the entire screen.");
        commandsArgs.put("exit", "no arguments\nStops all application.");

    }

    String exec(Parser parser) throws IOException, TerminalException {
        return exec(parser.getCmd(), parser.getArguments());
    }

    private String exec(String cmd, String[] args) throws IOException, TerminalException {
        switch (cmd) {
            case "cp":
                cp(args);
                return "";
            case "mv":
                mv(args);
                return "";
            case "rm":
                rm(args);
                return "";
            case "mkdir":
                mkdir(args);
                return "";
            case "rmdir":
                rmdir(args);
                return "";
            case "cat":
                return cat(args);
            case "cd":
                cd(args[0]);
                return "";
            case "more":
                if (args[0] == null)
                    moreText(args[1].split("\n"));
                else
                    more(args[0]);
                return "";
            case "pwd":
                return pwd();
            case "help":
                if (args.length == 0)
                    return help();
                else
                    return help(args[0]);
            case "args":
                if (args.length == 0)
                    return arg();
                else
                    return arg(args[0]);
            case "date":
                return date();
            case "ls":
                return ls(args);
            case "exit":
                throw new TerminalException(Main.stopApplicationMessage);
            case "clear":
                clear();
                return "";
            default:
                return "Should never be here.";
        }
    }

    private void clear() {
        for (int i = 0; i < 100; i++)
            out.println();
    }

    private String help(String arg) {
        return commandsArgs.get(arg);
    }

    private String help() {
        StringBuilder ret = new StringBuilder();
        for (Map.Entry<String, String> entry : commandsArgs.entrySet()) {
            ret.append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n\n");
        }
        return ret.substring(0, ret.length() - 2); // removes last 2 new lines
    }

    private String arg(String arg) {
        String command = help(arg);
        //First line of help is the arguments
        String[] split = command.split("[\\r\\n]+");
        return split[0];
    }

    private String arg() {
        String[] data = help().split("[\\r\\n]+");

        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < data.length; i += 2)
            ret.append(data[i]).append("\n");
        return ret.toString();
    }

    private void moreText(String[] data) throws TerminalException {
        final int pageSize = 10;
        int lastLine = 0, display = pageSize;
        while (true) {
            if (lastLine < data.length) {
                int toLine = min(display + lastLine, data.length);
                for (int i = lastLine; i < toLine; ++i) {
                    out.println(data[i]);
                }
                lastLine = toLine;
            }
            out.printf("Displayed %.2f%% of text, Space: Display next page, Enter: Display next line, b: Display previous page , q : exit : ",
                    ((float) (lastLine) / data.length * 100));
            String c = in.nextLine();
            if (c.equals(" ")) {
                display = pageSize;
            } else if (c.equals("b")) {
                display = pageSize;
                lastLine = max(0, lastLine - pageSize * 2);
            } else if (c.isEmpty()) {
                display = 1;
            } else if (c.equals("q")) {
                break;
            } else
                throw new TerminalException("Unsupported input for argument more");

        }
    }

    private void more(String arg) throws TerminalException {
        try {

            List<String> data = Files.readAllLines(Paths.get(arg));
            moreText(data.toArray(new String[0]));
        } catch (IOException ex) {
            throw new TerminalException(String.format("more: cannot read %s: IO error", Paths.get(arg).getFileName()));
        }
    }

    private void cp(Path sourcePath, Path destinationPath, String cmd) throws TerminalException {
        Path dest = destinationPath;
        //if no filename is given, but is needed, put it.
        if (destinationPath.toFile().isDirectory())
            dest = destinationPath.resolve(sourcePath.getFileName());

        if (sourcePath.toFile().isDirectory()) {
            System.out.println(String.format("%s: Directory %s was ignored.", cmd, sourcePath.getFileName()));
        } else if (sourcePath.toFile().isFile()) {
            try {
                Files.copy(sourcePath, dest, new StandardCopyOption[]{StandardCopyOption.REPLACE_EXISTING});
            } catch (Exception ignored) {
                throw new TerminalException(String.format("%s: cannot copy %s: IO error", cmd, sourcePath.getFileName()));
            }
        } else {
            throw new TerminalException(String.format("%s: cannot copy %s, no such file or directory.", cmd, sourcePath.getFileName()));
        }
    }

    private void cp(String[] args) throws TerminalException {
        cp(args, "cp");
    }

    private void cp(String[] args, String cmd) throws TerminalException {
        Path destinationDir = Paths.get(args[args.length - 1]);

        if (args.length > 2 && !destinationDir.toFile().isDirectory()) {
            throw new TerminalException(String.format("%s: target %s is not a directory.", cmd, destinationDir.getFileName()));
        }
        for (int i = 0; i < args.length - 1; ++i) {
            cp(Paths.get(args[i]), destinationDir, cmd);
        }
    }

    private void mv(String[] args) throws TerminalException {
        cp(args, "mv");
        try {
            rm(Arrays.copyOfRange(args, 0, args.length - 1));
        } catch (Exception ignored) {
        }

    }

    private void rm(String[] args) throws TerminalException {
        for (String arg : args) {
            File f = new File(arg);
            if (f.isFile())
                try {
                    Files.delete(f.toPath());
                } catch (Exception ignored) {
                    throw new TerminalException(String.format("rm: cannot remove %s: IO error.", f.getName()));
                }
            else if (f.isDirectory()) {
                throw new TerminalException(String.format("rm: cannot remove %s: Is a directory.", f.getName()));
            } else {
                throw new TerminalException(String.format("rm: cannot remove %s: No such file or directory.", f.getName()));
            }
        }
    }

    private void rmdir(String[] args) throws TerminalException {
        for (String arg : args) {
            File f = new File(arg);
            if (f.isFile()) {
                throw new TerminalException(String.format("rmdir: cannot remove %s: Not a directory.", f.getName()));
            } else if (f.isDirectory()) {
                try {
                    Files.delete(Paths.get(arg));
                } catch (DirectoryNotEmptyException ignored) {
                    throw new TerminalException(String.format("rmdir: cannot remove %s: Directory not empty.", f.getName()));
                } catch (IOException ex) {
                    throw new TerminalException(String.format("rmdir: cannot remove %s: IO error.", f.getName()));
                }
            } else {
                throw new TerminalException(String.format("rmdir: cannot remove %s: No such file or directory.", f.getName()));
            }
        }

    }

    private void mkdir(String[] args) throws TerminalException {
        for (String arg : args) {
            File f = new File(arg);
            if (f.exists()) {
                throw new TerminalException(String.format("mkdir: cannot create directory %s: File exists.", f.getName()));
            }
            if (!f.mkdirs()) {
                throw new TerminalException(String.format("mkdir: cannot create directory %s: IO error.", f.getName()));
            }
        }
    }

    private String ls(String[] args) throws TerminalException {
        List<String> names = new ArrayList<>();

        for (String arg : args) {
            File f = new File(arg);
            if (f.isFile()) {
                names.add(f.getName());
            } else if (f.isDirectory()) {
                if (args.length > 1)
                    names.add(": " + f.getName());
                File[] sub = f.listFiles();
                if (sub == null)
                    throw new TerminalException(String.format("ls: cannot retrieve %s: IO error.", f.getName()));
                for (File file : sub) {
                    names.add(((args.length > 1) ? "     " : "") + (file.isDirectory() ? ": " : " ") + file.getName());
                }
            } else {
                throw new TerminalException(String.format("ls: cannot access %s: No such file or directory.", f.getName()));
            }
        }
        return String.join("\n", names);
    }

    private String pwd() {
        return Main.workingDir;
    }

    private String cat() {
        out.println("Accepting input from user: (type '<stop>' to terminate)");
        StringBuilder sb = new StringBuilder();
        String currentLine;
        while (true) {
            currentLine = in.nextLine();
            if (currentLine.equals("<stop>"))
                break;
            sb.append(currentLine);
            sb.append("\n");
        }
        return sb.toString();
    }

    private String cat(String[] args) throws TerminalException {
        if (args == null || args.length == 0)
            return cat();

        Charset charset = StandardCharsets.UTF_8;
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            File f = new File(arg);
            if (f.isDirectory()) {
                throw new TerminalException(String.format("cat: %s is a directory.", f.getName()));
            } else if (!f.isFile()) {
                throw new TerminalException(String.format("cat: %s no such file or directory.", f.getName()));
            } else {
                try {

                    List<String> data = Files.readAllLines(f.toPath(), charset);
                    for (String line : data) {
                        sb.append(line);
                        sb.append('\n');
                    }
                } catch (IOException ignored) {
                    throw new TerminalException(String.format("cat: %s IO error.", f.getName()));
                }
            }
        }
        return sb.toString();
    }

    private void cd(String newDir) throws TerminalException {
        File tester = new File(newDir);
        if (tester.isDirectory() && tester.exists())
            Main.workingDir = newDir;
        else
            throw new TerminalException(String.format("cd: %s no such file or directory.", tester.getName()));
    }

    private String date() {
        Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH).format(calendar.getTime().getTime());
    }
}