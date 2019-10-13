import javax.sound.sampled.Line;
import javax.swing.plaf.basic.BasicLookAndFeel;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


public class Terminal {

    private static final String homeDir = System.getProperty("user.dir");
    private String workingDir;
    private Scanner in;
    private PrintStream out;

    public Terminal(InputStream input, PrintStream output) {
        this(homeDir, input, output);

    }

    public Terminal(String workingDirectory, InputStream input, PrintStream output) {
        workingDir = workingDirectory;
        in = new Scanner(input);
        out = output;
    }

    /*
        * TODO: figure out how to do it: clear : clear the screen
        ls : list files and directories in a path
        mkdir : make directory
        more/less :
        cat
        pipe
        >
        >>
        args : display arguments of command
        help
    */

    public String exec(String cmd, String[] args) throws IOException, TerminalException {
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
                //TODO: do fucking stuff
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
                return arg(args[0]);
            case "date":
                return date();
            case "ls":
                if (args.length == 0)
                    return ls();
                else
                    return ls(args[0]);
            default:
                return "Should never be here.";
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
        String directory = path.substring(0, nLastBackslash + 1);
        Path parent = getAbsolutePath(directory);
        String[] ret = parent.toFile().list();
        if (ret == null)
            throw new TerminalException("No files found.");
        else {
            List<Path> res = new ArrayList<>();
            for (String cur : ret) {
                Path current = Paths.get(cur);
                if (!current.toFile().isDirectory() && current.getFileName().toString().matches(fileName)) {
                    res.add(current);
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
            Files.copy(getAbsolutePath(cur), getAbsolutePath(destinationPath), new StandardCopyOption[]{REPLACE_EXISTING});
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
        if (type == Parser.PathType.MultipleFiles || type == Parser.PathType.SingleFile) {
            rm(decompose(sourcePath));
        } else {
            File current = new File(sourcePath);
            String[] files = current.list();
            if (files == null) {
                throw new TerminalException("Directory does not exist.");
            }
            if (files.length == 0)
                rm(new Path[]{Paths.get(sourcePath)});

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
                for (String file : files) {
                    rm(file, true);
                }
            }
        }
    }

    private String ls() throws TerminalException {
        return ls(workingDir);
    }

    private String ls(String sourcePath) throws TerminalException {
        File[] directories = getAbsolutePath(sourcePath).toFile().listFiles();
        if (directories == null)
            throw new TerminalException("Directory does not exist.");

        List<String> names = new ArrayList<>();
        for (File file : directories) {
            names.add(file.getName());
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
        //TODO: read input from user and return it
    }

    private String cat(Path[] paths) throws IOException {
        File temp = new File("temp.txt");
        Charset charset = StandardCharsets.UTF_8;
        for (Path file : paths) {
            List<String> data = Files.readAllLines(file, charset);
            Files.write(temp.toPath(), data, charset, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        // TODO: temp contains the output so think how to display it
    }

    private void cd(String newDir) throws TerminalException {
        Path abs = getAbsolutePath(newDir);
        File tester = abs.toFile();
        if (tester.isDirectory() && tester.exists())
            workingDir = newDir;
        else
            throw new TerminalException("Directory does not exist.");
    }

    private String date() {
        Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH).format(calendar.getTime().getTime());
    }

    public String getWorkingDirectory() {
        return workingDir;
    }
// Add any other required command in the same structure…..
}