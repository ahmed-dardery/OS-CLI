//TODO: do all the todos

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Parser {
    String[] args; // Will be filled by arguments extracted by parse method
    String cmd; // Will be filled by the command extracted by parse method


    // Returns true if it was able to parse user input correctly. Otherwise false
// In case of success, it should save the extracted command and arguments
// to args and cmd variables
// It should also print error messages in case of too few arguments for a commands
// eg. “cp requires 2 arguments”
    /*
     */
    final static List<String> supportedCommands = Arrays.asList("cp", "mv", "rm", "pwd", "cat", "cd", "mkdir", "rmdir", "more", "args", "date", "help");

    public boolean parse(String input) throws ParsingException {
        //TODO: handle multiple spaces and tabs Unless they are between quotations
        //TODO: Modify error messages to be as linux
        //TODO: Handle pipes and redirection > >>
        String[] data = input.split(" ");

        if (data.length == 0 || data[0].length() == 0)
            throw new ParsingException("Empty Command");

        if (!supportedCommands.contains(data[0]))
            throw new ParsingException("Command " + data[0] + " is unsupported");
        cmd = data[0];
        args = Arrays.copyOfRange(data, 1, data.length);
        return CheckCmd();
    }

    private boolean CheckCmd() throws ParsingException {
        if (cmd.equals("pwd") || cmd.equals("date"))
            return args.length == 0;

        if (cmd.equals("help") && args.length == 0)
            return true;

        if (cmd.equals("help") || cmd.equals("args"))
            return args.length == 1 && supportedCommands.contains(args[0]);

        if (cmd.equals("ls")) {
            if (args.length == 0) return true;
            else if (args.length > 1) return false;
            return IdentifyPath(args[0]) != PathType.Invalid;
        }
        if (cmd.equals("cd")) {
            if (args.length == 0) return true;
            else
                return (args.length == 1) && IdentifyPath(args[0]) == PathType.Directory;
        }
        //all future commands require at least 1 parameter
        if (args.length == 0) return false;

        if (cmd.equals("mv") || cmd.equals("cp")) {
            if (args.length != 2) return false;
            PathType t1 = IdentifyPath(args[0]);
            PathType t2 = IdentifyPath(args[1]);
            if (t1 == PathType.Invalid || t2 == PathType.Invalid) return false;
            if (t2 == PathType.MultipleFiles) return false;

            if (t1 == PathType.Directory || t1 == PathType.MultipleFiles)
                return t2 == PathType.Directory;
        }
        if (cmd.equals("more"))
            return IdentifyPath(args[0]) == PathType.SingleFile;

        if (cmd.equals("mkdir") || cmd.equals("rmdir"))
        {
            for (String item : args) {
                if (IdentifyPath(item) != PathType.Directory) return false;
            }
            return true;
        }
        if (cmd.equals("cat"))
        {
            for (String item : args) {
                if (IdentifyPath(item) != PathType.MultipleFiles && IdentifyPath(item) != PathType.SingleFile ) return false;
            }
            return true;
        }
        return false;

    }

    enum PathType {
        SingleFile, MultipleFiles, Directory, Invalid
    }

    private PathType IdentifyPath(String path) {
        //TODO: implement
        /*
            __file specials__
            * wild card

            __directory specials__

            ~ home
            .. go back one
         */
        return PathType.Invalid;
    }


    public String getCmd() {
        return cmd;
    }

    public String[] getArguments() {
        return args;
    }
}