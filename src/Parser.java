import java.io.File;
import java.nio.file.InvalidPathException;
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
        SingleFile, MultipleFiles, Directory, Invalid
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
    final static private List<String> supportedCommands = Arrays.asList("cp", "mv", "rm", "pwd", "cat", "cd", "mkdir", "rmdir", "more", "args", "date", "help" , "clear","exit");

    public static Parser[] parseUserInput(String input) throws ParsingException {
        String[] res = input.split("\\|");
        ArrayList<Parser> commands = new ArrayList<>();
        for (String entry : res) {
            commands.add(new Parser(entry));
        }
        return commands.toArray(new Parser[0]);
    }

    private Parser(String input) throws ParsingException {
        input = input.trim();
        if (input.length() == 0){
            throw new ParsingException("");
        }
        String[] res = input.split(">>|>");
        //Split does not put trailing empty strings, so I need to check if the last character is >
        if (input.charAt(input.length() - 1)=='>') {
            throw new ParsingException("Invalid redirection syntax.");
        }

        res[0] = res[0].trim();

        if (res.length > 2)
            throw new ParsingException("Cannot redirect output to multiple files.");
        if (res.length == 2) {
            res[1] = res[1].trim();
            if (isFileOrDirectory(res[1]) != PathType.SingleFile)
                throw new ParsingException(String.format("%s is not a valid file for redirection.", res[1]));

            redirectionFilename = res[1];

            if (input.contains(">>"))
                redirectionType = RedirectionType.Append;
            else
                redirectionType = RedirectionType.Truncate;
        } else
            redirectionType = RedirectionType.NoRedirection;

        String[] data = splitInput(res[0]);

        if (data.length == 0 || data[0].length() == 0)
            throw new ParsingException("Empty Command");

        cmd = data[0];
        args = Arrays.copyOfRange(data, 1, data.length);

        CheckCommandAndArguments(cmd, args);
    }

    private static String[] splitInput(String subjectString) {
        List<String> matchList = new ArrayList<>();

        //Explanation of regex:
        //[^"'\s]+ matches one or more of anything but whitespace or quotations
        //"[^"]*" matches double quotation marks around zero or more of anything but double quotations
        //'[^']*' matches single quotation marks around zero or more of anything but single quotations
        // | makes it match any of the three matchers.
        Pattern regex = Pattern.compile("[^\"'\\s]+|\"[^\"]*\"|'[^']*'");
        Matcher regexMatcher = regex.matcher(subjectString);
        while (regexMatcher.find()) {
            matchList.add(regexMatcher.group());
        }
        return matchList.toArray(new String[0]);
    }

    private static void CheckCommandAndArguments(String cmd, String[] args) throws ParsingException {
        //'ls', 'cd', 'help' and 'cat' can work with no arguments
        if ((cmd.equals("help") || cmd.equals("ls") || cmd.equals("cd") || cmd.equals("cat")) && args.length == 0)
            return;

        switch (cmd) {
            case "pwd":
            case "date":
                if (args.length != 0)
                    throw new ParsingException("Command " + cmd + " does not support any arguments.");
                return;

            case "help":
                if (args.length>1) throw new ParsingException(String.format("Command %s does not support %d arguments.", cmd, args.length));
                return;
            case "args":
                if (args.length > 1)
                    throw new ParsingException(String.format("Command %s does not support %d arguments.", cmd, args.length));
                if (args.length != 0 &&!supportedCommands.contains(args[0]))
                    throw new ParsingException(String.format("%s is not a supported command.", args[0]));

                return;
            case "ls":
                if (args.length > 1)
                    throw new ParsingException(String.format("Command %s does not support %d arguments.", cmd, args.length));
                else if (IdentifyPath(args[0]) == PathType.Invalid)
                    throw new ParsingException(String.format("%s is not a valid path", args[0]));
                return;
            case "cd":
                if (args.length > 1)
                    throw new ParsingException(String.format("Command %s does not support %d arguments.", cmd, args.length));
                else if (IdentifyPath(args[0]) != PathType.Directory)
                    throw new ParsingException(String.format("%s is not a valid directory", args[0]));
                return;
            case "mv":
            case "cp":
                if (args.length != 2)
                    throw new ParsingException(String.format("Command %s does not support %d arguments.", cmd, args.length));

                PathType t1 = IdentifyPath(args[0]);
                PathType t2 = IdentifyPath(args[1]);
                if (t1 == PathType.Invalid)
                    throw new ParsingException(String.format("%s is not a valid directory", args[0]));
                else if (t2 == PathType.Invalid)
                    throw new ParsingException(String.format("%s is not a valid directory", args[1]));

                if (t2 == PathType.MultipleFiles)
                    throw new ParsingException(String.format("%s is not a valid destination", args[1]));

                if ((t1 == PathType.Directory || t1 == PathType.MultipleFiles) && t2 != PathType.Directory)
                    throw new ParsingException(String.format("%s is not a valid destination", args[1]));

                return;
            case "more":
                if (IdentifyPath(args[0]) != PathType.SingleFile)
                    throw new ParsingException(String.format("%s is not a valid file", args[0]));

                return;
            case "mkdir":
            case "rmdir":
                for (String item : args) {
                    if (IdentifyPath(item) != PathType.Directory)
                        throw new ParsingException(String.format("%s is not a valid directory", item));
                }
                return;
            case "cat":
                for (String item : args) {
                    if (IdentifyPath(item) != PathType.MultipleFiles && IdentifyPath(item) != PathType.SingleFile)
                        throw new ParsingException(String.format("%s is not a valid file", item));
                }
                return;
            case "rm":
                if (args.length != 1)
                    throw new ParsingException("rm only supports 1 argument.");
                if (IdentifyPath(args[0]) == PathType.Invalid)
                    throw new ParsingException(String.format("%s is not a valid file", args[0]));
                return;
            default:
                throw new ParsingException(String.format("%s is not a valid command", cmd));
        }
    }

    private static boolean isValidMultipleFiles(String str) {
        //matches any valid filename, including *.ext, name.* and *.*
        return str.matches("(\\*|[^<>:\"/\\\\|?*\0]+)\\.(\\*|[^<>:\"/\\\\|?*\0]+)");
    }

    private static PathType isFileOrDirectory(String str) {
        try {
            Paths.get(str);
            File f = new File(str);
            if (f.getName().contains(".")) return PathType.SingleFile;
            else return PathType.Directory;

        } catch (InvalidPathException | NullPointerException ignored) {
            return PathType.Invalid;
        }

    }

    static PathType IdentifyPath(String path) {
        //Handles empty paths and paths between quotations
        if (path == null || path.length() == 0) return PathType.Invalid;
        char begin = path.charAt(0), end = path.charAt(path.length() - 1);
        if (path.length() == 1 && (begin == '\'' || begin == '"')) return PathType.Invalid;
        if ((begin == '\'' && end == '\'') || (begin == '"' && end == '"')) {
            if (path.length() > 2)
                path = path.substring(1, path.length() - 1);
            else
                return PathType.Invalid;
        }

        if (path.equals("~") || path.equals("..")) return PathType.Directory;

        PathType initial = isFileOrDirectory(path);
        if (initial != PathType.Invalid) return initial;

        int nLastBackslash = path.lastIndexOf('\\');

        String fileName = path.substring(nLastBackslash + 1);
        String directory = path.substring(0, nLastBackslash + 1);

        if (isFileOrDirectory(directory) != PathType.Invalid && isValidMultipleFiles(fileName))
            return PathType.MultipleFiles;
        else
            return PathType.Invalid;
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