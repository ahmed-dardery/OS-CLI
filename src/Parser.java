import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private String[] args; // Will be filled by arguments extracted by parse method
    private String cmd; // Will be filled by the command extracted by parse method
    private RedirectionType redirectionType;
    private String redirectionFilename;

    enum PathType {
        Singleton, Wildcard, Invalid
    }

    enum RedirectionType {
        NoRedirection,
        Append,
        Truncate
    }

    // Returns true if it was able to parse user input correctly. Otherwise false
    // In case of success, it should save the extracted command and arguments
    // to args and cmd variables
    // It should also print error messages in case of too few arguments for a commands
    // eg. “cp requires 2 arguments”
    final static private List<String> supportedCommands = Arrays.asList("ls", "cp", "mv", "rm", "pwd", "cat", "cd", "mkdir", "rmdir", "more", "args", "date", "help", "clear", "exit");
    final static private List<String> nopathCommands = Arrays.asList("pwd", "more", "args", "date", "help", "clear", "exit");

    public Parser(String input, String givenInput) throws ParsingException {
        input = input.trim();
        if (input.length() == 0) {
            throw new ParsingException("");
        }
        String[] res = input.split(">>|>");
        //Split does not put trailing empty strings, so I need to check if the last character is >
        if (input.charAt(input.length() - 1) == '>') {
            throw new ParsingException("dabsh: invalid syntax near redirection symbol");
        }

        res[0] = res[0].trim();

        if (res.length > 2)
            throw new ParsingException("dabsh: current version does not support multiple redirection.");
        if (res.length == 2) {
            res[1] = res[1].trim();
            if (IdentifyPath(res[1]) != PathType.Singleton)
                throw new ParsingException(String.format("dabsh: %s is not a valid file for redirection.", res[1]));

            redirectionFilename = getAbsolutePath(res[1]);
            if (input.contains(">>"))
                redirectionType = RedirectionType.Append;
            else
                redirectionType = RedirectionType.Truncate;
        } else
            redirectionType = RedirectionType.NoRedirection;

        String[] data = splitInput(res[0]);

        if (data.length == 0 || data[0].length() == 0)
            throw new ParsingException("dabsh: empty commands cannot be redirected to files.");

        cmd = data[0];
        args = Arrays.copyOfRange(data, 1, data.length);
        if (!nopathCommands.contains(cmd)) {
            ArrayList<String> completeargs = new ArrayList<>();
            for (String arg : args) {
                switch (IdentifyPath(arg)) {
                    case Invalid:
                        throw new ParsingException("dabsh: invalid path given in arguments");
                    case Wildcard:
                        completeargs.addAll(decomposeWildCard(arg));
                        break;
                    case Singleton:
                        completeargs.add(getAbsolutePath(arg));
                        break;
                }
            }
            args = completeargs.toArray(new String[0]);
        }
        if (givenInput == null)
            CheckCommandAndArguments();
        else if (cmd.equals("more") && args.length == 0) {
            args = new String[]{null, givenInput};
        }
    }

    public Parser(String input) throws ParsingException {
        this(input, null);
    }

    private static String[] splitInput(String subjectString) {
        List<String> matchList = new ArrayList<>();

        //Explanation of regex:
        //[^"'\s]+ matches one or more of anything but whitespace or quotations
        //"[^"]*" matches double quotation marks around zero or more of anything but double quotations
        //'[^']*' matches single quotation marks around zero or more of anything but single quotations
        // | makes it match any of the three matchers.
        Pattern regex = Pattern.compile("[^\"'\\s]+|\"([^\"]*)\"|'([^']*)'");
        Matcher regexMatcher = regex.matcher(subjectString);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null)
                matchList.add(regexMatcher.group(1));
            else if (regexMatcher.group(2) != null)
                matchList.add(regexMatcher.group(2));
            else
                matchList.add(regexMatcher.group(0));
        }
        return matchList.toArray(new String[0]);
    }

    private void CheckCommandAndArguments() throws ParsingException {
        switch (cmd) {
            case "date":
            case "clear":
            case "exit":
            case "pwd":
                if (args.length > 0)
                    throw new ParsingException(String.format("%s: too many arguments.", cmd));
                return;
            case "cd":
                if (args.length == 0)
                    args = new String[]{Main.workingDir};
                if (args.length > 1)
                    throw new ParsingException(String.format("%s: too many arguments.", cmd));
                return;
            case "help":
            case "args":
                if (args.length > 1)
                    throw new ParsingException(String.format("%s: too many arguments.", cmd));
                if (args.length != 0 && !supportedCommands.contains(args[0]))
                    throw new ParsingException(String.format("%s: %s: unsupported command.", cmd, args[0]));
                return;
            case "more":
                if (args.length != 1)
                    throw new ParsingException(String.format("%s: only one argument is supported.", cmd));
                return;
            case "mkdir":
            case "rmdir":
            case "rm":
                if (args.length < 1)
                    throw new ParsingException(String.format("%s: too few arguments.", cmd));
                return;
            case "cp":
            case "mv":
                if (args.length == 0)
                    throw new ParsingException(String.format("%s: too few arguments.", cmd));
                else if (args.length == 1)
                    throw new ParsingException(String.format("%s: destination argument missing.", cmd));
            case "ls":
                if (args.length == 0)
                    args = new String[]{Main.workingDir};
                return;
            case "cat":
                return;
            default:
                throw new ParsingException(String.format("%s: unable to parse this particular command.", cmd));

        }
    }

    private static boolean isValidWildCard(String filename) {
        return (filename.matches("[^<>:\"/\\\\|?]+"));
    }

    private static boolean isValidSingletonPath(String path) {
        try {
            Paths.get(path);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static PathType IdentifyPath(String path) {
        if (path == null || path.length() == 0) return PathType.Invalid;

        if (path.equals("~") || path.equals("..") || isValidSingletonPath(path)) return PathType.Singleton;

        int nLastBackslash = path.lastIndexOf('\\');

        String fileName = path.substring(nLastBackslash + 1);
        String directory = path.substring(0, nLastBackslash + 1);

        if (isValidSingletonPath(directory) && isValidWildCard(fileName))
            return PathType.Wildcard;
        else
            return PathType.Invalid;
    }

    private List<String> decomposeWildCard(String path) throws ParsingException {
        int nLastBackslash = path.lastIndexOf('\\');

        String fileName = path.substring(nLastBackslash + 1);
        fileName = fileName.replaceAll("\\*", ".*");
        String directory = path.substring(0, nLastBackslash + 1);
        Path parent = Paths.get(getAbsolutePath(directory));
        File[] ret = parent.toFile().listFiles();
        if (ret == null)
            throw new ParsingException("dabsh: No files found.");
        else {
            List<String> res = new ArrayList<>();
            for (File current : ret) {

                if (current.toPath().getFileName().toString().matches(fileName)) {
                    res.add(current.toString());
                }
            }
            if (res.size() == 0)
                throw new ParsingException("dabsh: No files found.");
            else
                return res;
        }
    }

    private String getAbsolutePath(String pathString) {
        if (pathString.equals("~"))
            return Main.homeDir;
        else if (pathString.equals("..")) {
            Path tmp = Paths.get(Main.workingDir).getParent();
            if (tmp == null)
                return Main.workingDir;
            else
                return tmp.toString();
        }

        Path path = Paths.get(pathString);
        if (path.isAbsolute()) {
            return path.toString();
        } else {
            return Paths.get(Main.workingDir, pathString).toString();
        }
    }


    public String getCmd() {
        return cmd;
    }

    public String[] getArguments() {
        return args;
    }

    public RedirectionType getRedirectionType() {
        return redirectionType;
    }

    public String getRedirectionFilename() {
        return redirectionFilename;
    }
}