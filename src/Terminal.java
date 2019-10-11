import javax.sound.sampled.Line;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Terminal {
    String workingDir;
    /*
        * TODO: figure out how to do it: clear : clear the screen
        * TODO: Handle Shortpath
cd : change directory
ls : list files and directories in a path
mkdir : make directory
rmdir : remove directory
more/less :
pipe
>
>>
args : display arguments of command
date
help
Handling Shortpath/full path
 */

    public void cp(Path sourcePath, Path destinationPath) throws IOException {
        Files.copy(sourcePath, destinationPath, new StandardCopyOption[]{REPLACE_EXISTING});
    }
    public List<String> ls (String sourcePath)
    {
//       File[] directories = new File(sourcePath).listFiles(File::isDirectory);
       File[] directories = new File(sourcePath).listFiles();
       List<String> names = new ArrayList<>();
       for (File file:directories)
       {
            names.add(file.getName());
       }
        return names;
    }
    public void mkdir (String dirName)
    {
        new File(workingDir+"/"+dirName).mkdir();
    }
    public void rmdir (String dirName)
    {
        File dir = new File(dirName);
        String[] files = dir.list();
        for (String current : files)
        {
            (new File(dir.getPath(),current)).delete();
        }
        dir.delete();
    }
    public void mv(Path sourcePath, Path destinationPath) throws IOException {
        Files.move(sourcePath, destinationPath, new StandardCopyOption[]{REPLACE_EXISTING});
    }

    public void rm(Path sourcePath) throws IOException {
        Files.delete(sourcePath);
    }

    public String pwd() {
        return workingDir;
    }

    public void cat(Path[] paths) throws IOException {
        File temp = new File ("temp.txt");
        Charset charset = StandardCharsets.UTF_8;
        for (Path file : paths)
        {
            List<String> data = Files.readAllLines(file,charset);
            Files.write(temp.toPath(),data,charset, StandardOpenOption.CREATE,StandardOpenOption.APPEND);
        }
        // TODO: temp contains the output so think how to display it
    }
    public void cd (String newDir)
    {
        workingDir = newDir;
    }
    public String Date ()
    {
        Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH).format(calendar.getTime().getTime());
    }
// Add any other required command in the same structure…..
}