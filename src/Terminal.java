import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;


class Terminal {

    private static final String homeDir = System.getProperty("user.dir");
    private String workingDir;
    private Scanner in;
    private PrintStream out;
    private Map<String, String> commandsArgs = new HashMap<>();

    Terminal(InputStream input, PrintStream output) {
        this(homeDir, input, output);
    }

    private Terminal(String workingDirectory, InputStream input, PrintStream output) {
        workingDir = workingDirectory;
        in = new Scanner(input);
        out = output;

        commandsArgs.put("cat","arg1: file arg2.n: files\nReads from and concatanate all files specified in the arguments.");
        commandsArgs.put("cd","[arg1: new_dir]\nChanges current working directory to new_dir, if omitted changes it to Home Directory.");
        commandsArgs.put("mkdir","arg1 : dir\nCreates an empty directory in path dir.");
        commandsArgs.put("rmdir","arg1 : dir\nRemoves directory at path dir, directory should be empty.");
        commandsArgs.put("more","arg1: file_path\nDisplays some of data in file_path, supports scrolling by Enter: next line, Space: one page, b: back one page.");
        commandsArgs.put("date","no arguments\nPrints current system date and time.");
        commandsArgs.put("args","[arg1: argument]\nDisplays arguments of the command specified in arg1.");
        commandsArgs.put("help","[arg1: argument]\nDisplays info about arg1, if omitted displays info about all commands.");
        commandsArgs.put("cp","arg1: file/dir arg2: file/dir\nCopies file/directory from arg1 to arg2.");
        commandsArgs.put("mv","arg1: file/dir arg2: file/dir\nMoves file/directory from arg1 to arg2.");
        commandsArgs.put("rm","arg1: file/Empty dir\nRemoves file/directory permanently.");
        commandsArgs.put("pwd","no arguments\nDisplays the current working directory.");
        commandsArgs.put("ls","[arg1: Directory]\nDisplays the files and subfolders in a directory , default: current working directory.");
        commandsArgs.put("clear","no arguments\nClears the entire screen.");
        commandsArgs.put("exit","no arguments\nStops all application.");

    }

    String exec(Parser parser) throws IOException, TerminalException {
        return exec(parser.getCmd(), parser.getArguments());
    }

    private String exec(String cmd, String[] args) throws IOException, TerminalException {
        switch (cmd) {
            case "cp":
                cp(args[0], args[1]);
                return "";
            case "mv":
                mv(args[0], args[1]);
                return "";
            case "rm":
                rm(args[0], false);
                return "";
            case "mkdir":
                mkdir(args[0]);
                return "";
            case "rmdir":
                rmdir(args[0]);
                return "";
            case "cd":
                if (args.length == 0)
                    cd(homeDir);
                else
                    cd(args[0]);
                return "";
            case "cat":
                if (args.length == 0)
                    return cat();
                else
                    return cat(args);
            case "more":
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
                if (args.length == 0)
                    return ls();
                else
                    return ls(args[0]);
            case "exit":
                throw new TerminalException(Main.stopApplicationMessage);
            default:
                return "Should never be here.";
        }
    }

    private String help(String arg) throws TerminalException {
        String ret = commandsArgs.get(arg);
        if (ret == null) throw new TerminalException("Argument is not exist");
        return ret;
    }

    private String help() {
        String ret = "";
        for (Map.Entry<String, String> entry : commandsArgs.entrySet()) {
            ret+= (entry.getKey() + " -> " + entry.getValue() + "\n\n");
        }
        return ret.substring(0,ret.length()-2); // removes last 2 \n
    }

    private String arg(String arg) throws TerminalException {
        String command = help(arg);
        String[] split = command.split("[\\r\\n]+");
        return split[0];
    }
    private String arg() {
        String[] data = help().split("[\\r\\n]+");
        String ret = "";
        for (int i = 0 ; i<data.length ; i+=2) ret+=(data[i]+"\n");
        return ret ;
    }

    private void more(String arg) throws IOException, TerminalException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(getAbsolutePath(arg).toString()));
        List<String> data = new ArrayList<>();
        String s;
        while ((s = bufferedReader.readLine()) != null) {
            data.add(s);
        }
        bufferedReader.close();
        final int pageSize = 50;
        int lastLine = 0 , display = pageSize;
        while (true) {
            if (lastLine < data.size()) {
                int toLine = min(display+lastLine,data.size());
                for (int i = lastLine; i <toLine; ++i) {
                    out.println(data.get(i));
                }
                lastLine = toLine;
            }
            out.printf("Displayed %.2f%% of text, Space: Display next page, Enter: Display next line, b: Display previous page , q : exit : ", ((float) (lastLine) / data.size() * 100));
            String c = in.nextLine();
//            if (c == ' '){
//                display = pageSize;
//            }
//            else if (c == 'b'){
//                display = pageSize;
//                lastLine = max(0,lastLine-pageSize*2);
//            }
//            else if (c == '\n'){
//                display = 1;
//            }
//            else if (c == 'q'){
//                break;
//            }
//            else
//                throw new TerminalException("Unsupported input for argument more");

            if (c.equals(" ")){
                display = pageSize;
            }
            else if (c.equals("b")){
                display = pageSize;
                lastLine = max(0,lastLine-pageSize*2);
            }
            else if (c.isEmpty()){
                display = 1;
            }
            else if (c.equals("q")){
                break;
            }
            else
                throw new TerminalException("Unsupported input for argument more");

        }


    }

    private Path getAbsolutePath(String pathString) {
        if (pathString.equals("~"))
            return Paths.get(homeDir);
        else if (pathString.equals(".."))
            return Paths.get(workingDir).getParent();

        Path path = Paths.get(pathString);
        if (path.isAbsolute()) {
            return path;
        } else {
            return Paths.get(workingDir, pathString);
        }
    }

    private Path getAbsolutePath(Path path) {
        return getAbsolutePath(path.toString());
    }

    private Path[] decompose(String path) throws TerminalException {
        int nLastBackslash = path.lastIndexOf('\\');

        String fileName = path.substring(nLastBackslash + 1);
        fileName = fileName.replaceAll("\\*", ".+");
        String directory = path.substring(0, nLastBackslash + 1);
        Path parent = getAbsolutePath(directory);
        File[] ret = parent.toFile().listFiles();
        if (ret == null)
            throw new TerminalException("No files found.");
        else {
            List<Path> res = new ArrayList<>();
            for (File current : ret) {

                if (!current.isDirectory() && current.toPath().getFileName().toString().matches(fileName)) {
                    res.add(current.toPath());
                }
            }
            if (res.size() == 0)
                throw new TerminalException("No files found.");
            else
                return res.toArray(new Path[0]);
        }
    }

    private void cp(Path[] sourcePaths, Path destinationPath) throws IOException {
        for (Path cur : sourcePaths) {
            Files.copy(getAbsolutePath(cur), getAbsolutePath(destinationPath), new StandardCopyOption[]{StandardCopyOption.REPLACE_EXISTING});
        }
    }

    private void cp(String sourcePath, String destinationPath) throws IOException, TerminalException {
        Parser.PathType type = Parser.IdentifyPath(sourcePath);
        if (type == Parser.PathType.MultipleFiles || type == Parser.PathType.SingleFile) {
            cp(decompose(sourcePath), Paths.get(destinationPath));
        } else {
            File current = new File(sourcePath);
            String[] files = current.list();
            if (files == null) {
                throw new TerminalException("File/Folder does not exist.");
            }
            for (String file : files) {
                cp(file, destinationPath);
            }
        }
    }

    private void mv(String sourcePath, String destinationPath) throws IOException, TerminalException {
        cp(sourcePath, destinationPath);
        rm(sourcePath, true);
    }

    private void rmdir(String dirName) throws IOException, TerminalException {
        rm(dirName, false);
    }

    private void rm(Path[] sourcePaths) throws IOException {
        for (Path cur : sourcePaths) {
            Files.delete(getAbsolutePath(cur));
        }
    }

    private void rm(String sourcePath, boolean removeRecursively) throws IOException, TerminalException {
        Parser.PathType type = Parser.IdentifyPath(sourcePath);
        Path src;
        if (type == Parser.PathType.MultipleFiles) {
            rm(decompose(sourcePath));
            return;
        } else {
            src = getAbsolutePath(sourcePath);
        }

        if (type == Parser.PathType.SingleFile) {
            rm(new Path[]{src});
        } else {
            File current = src.toFile();
            if (!current.isDirectory()) {
                throw new TerminalException("Directory does not exist.");
            }
            File[] files = current.listFiles();

            if (files == null || files.length == 0) {
                rm(new Path[]{src});
                return;
            }

            if (!removeRecursively) {
                out.println("This directory is not empty. Are you sure you want to remove this directory? (y/n)");
                char ans = in.next().charAt(0);
                if (ans == 'y')
                    removeRecursively = true;
                else if (ans != 'n') {
                    out.println("Invalid answer, so I will abort.");
                }
            }

            if (removeRecursively) {
                for (File file : files) {
                    rm(file.getPath(), true);
                }
                rm(new Path[]{src});
            }
        }
    }

    private String ls() throws TerminalException {
        return ls(workingDir);
    }

    private String ls(String sourcePath) throws TerminalException {
        List<String> names = new ArrayList<>();

        if (Parser.IdentifyPath(sourcePath) == Parser.PathType.MultipleFiles) {
            for (Path p : decompose(sourcePath)) {
                names.add(p.toString());
            }
        } else {
            File[] directories = getAbsolutePath(sourcePath).toFile().listFiles();
            if (directories == null)
                throw new TerminalException("Directory does not exist.");

            for (File file : directories) {

                names.add((file.isDirectory() ? ": " : " ") + file.getName());
            }
        }


        return String.join("\n", names);
    }

    private void mkdir(String dirName) throws TerminalException {
        Path p = getAbsolutePath(dirName);
        if (p.toFile().exists()) {
            throw new TerminalException("Directory already exists.");
        }
        if (!(p.toFile().mkdir())) {
            throw new TerminalException("Cannot make directory.");
        }
    }


    private String pwd() {
        return workingDir;
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

    private String cat(String[] paths) throws IOException{
        Charset charset = StandardCharsets.UTF_8;
        StringBuilder sb = new StringBuilder();
        for (String path : paths) {
            List<String> data = Files.readAllLines(Paths.get(path), charset);
            for (String line : data) {
                sb.append(line);
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    private void cd(String newDir) throws TerminalException {
        Path abs = getAbsolutePath(newDir);
        File tester = abs.toFile();
        if (tester.isDirectory() && tester.exists())
            workingDir = abs.toString();
        else
            throw new TerminalException("Directory does not exist.");
    }

    private String date() {
        Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH).format(calendar.getTime().getTime());
    }

    String getWorkingDirectory() {
        return workingDir;
    }
}

/*
final static private List<String> supportedCommands = Arrays.asList("cp", "mv", "rm", "pwd", "cat", "cd", "mkdir", "rmdir", "more", "args", "date", "help");

Usage: cp arg1:source_path arg2:destination_path
Copies one or more files from source_path to destination_path

mv arg1:source_path arg2:destination_path
Moves one or more files from source_path to destination_path

rm arg1:file_path
Removes one or more files specified by file_path

pwd
Prints current working directory

cat
Accepts input from user and prints it. Use >, or >> to redirect input to a file.

-OR-

cat arg1: file arg2.n: files
Reads from and concatanate all files specified in the arguments.

cd [arg1: new_dir]
Changes current working directory to new_dir, if omitted changes it to Home Directory

mkdir arg1 : dir
Creates an empty directory in path dir

rmdir arg1 : dir
Removes directory at path dir, directory should be empty.

more arg1: file_path
Displays some of data in file_path, supports scrolling by Enter: next line, Space: one page, b: back one page.

date
Prints current system date and time.

args arg1: argument
Displays arguments of the command sepecified in arg1

help [arg1: argument]
Displays info about arg1, if omitted displays info about all commands.
*/