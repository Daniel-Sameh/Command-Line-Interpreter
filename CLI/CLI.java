import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class CLI{
    private static String ERROR_MESSAGE = "An unexpceted error occured: ";
    private Path currentDirectory;
    public CLI(){
        currentDirectory = Paths.get("").toAbsolutePath();
    }
    public String getCurrentDirectory(){
        return currentDirectory.toString();
    }
    public String executeCommand(String command){
        String[] args = command.split("\\s+");
        String cmd = args[0];
        args = Arrays.copyOfRange(args, 1, args.length);
        switch (cmd) {
            case "pwd":
                return currentDirectory.toString();
            case "cd":
                return changeDirectory(args);
            case "ls":
                return listDirectory(args);
            case "mkdir":
                return createNewDirectory(args);
            case "rmdir":
                return removeDirectory(args);
            case "touch":
                return createNewFile(args);
            case "mv":
                return moveOrRename(args);
            case "rm":
                return removeFile(args);
            case "cat":
                return displayFileContents(args);
            default:
                return "Unknown command: " + cmd;
        }
    }
    private String changeDirectory(String[] args){
        if (args.length == 0){
            return "Usage: cd <directory>";
        }
        Path newPath = currentDirectory.resolve(args[0]).normalize();
        if (Files.exists(newPath) && Files.isDirectory(newPath)){
            currentDirectory = newPath;
            return "Directory changed: " + args[0];
        }
        return "Directory not found: " + args[0];
    }
    private String listDirectory(String[] args){
        Boolean hidden = false, reversed = false;
        for (String arg: args){
            if (arg.equals("-a"))
                hidden = true;
            else if (arg.equals("-r"))
                reversed = true;
            else if (arg.equals("-ar") || arg.equals("-ra"))
                hidden = reversed = true;
        }
        try{
            DirectoryStream<Path> stream = Files.newDirectoryStream(currentDirectory);
            ArrayList<String> list = new ArrayList<>();
            for (Path path: stream){
                String name = path.getFileName().toString();
                if (name.charAt(0) != '.' || hidden)
                    list.add(name);
            }
            stream.close();
            if (reversed)
                Collections.reverse(list);

            String listString = "";
            for (String name: list){
                listString += name + "\n";
            }
            return listString;
        }
        catch (IOException e){
            return ERROR_MESSAGE + e;
        }
    }
    private String createNewDirectory(String[] args){
        if (args.length == 0) {
            return "Usage: mkdir <directory>";
        }
        Path newDir = currentDirectory.resolve(args[0]);
        try{
            Files.createDirectories(newDir);
        }
        catch(FileAlreadyExistsException e){
            return "File already exists.";
        }
        catch(IOException e){
            return ERROR_MESSAGE + e;
        }
        return "Directory created: " + newDir;
    }
    private String removeDirectory(String[] args) {
        if (args.length == 0) {
            return "Usage: rmdir <directory>";
        }
        Path dir = currentDirectory.resolve(args[0]);
        if (!Files.exists(dir) || !Files.isDirectory(dir)){
            return "Directory not found: " + dir;
        }
        try{
            Files.deleteIfExists(dir);
        }
        catch (IOException e){
            return ERROR_MESSAGE + e;
        }
        return "Directory removed: " + dir;
    }
    private String createNewFile(String[] args){
        if (args.length == 0) {
            return "Usage: touch <file>";
        }
        Path file = currentDirectory.resolve(args[0]);
        try{
            Files.createFile(file);
        }
        catch (FileAlreadyExistsException e){
            return "File already exists: " + file;
        }
        catch (IOException e){
            return ERROR_MESSAGE + e;
        }
        return "File created: " + file;
    }
    private String moveOrRename(String[] args){
        if (args.length < 2) {
            return "Usage: mv <source> <destination>";
        }
        Path source = currentDirectory.resolve(args[0]);
        Path destination = currentDirectory.resolve(args[1]);
        if (!Files.exists(source)){
            return "Source not found: " + args[0];
        }
        if (Files.isDirectory(destination)) {
            destination = destination.resolve(source.getFileName());
        }
        try {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return "Error moving/renaming: " + e.getMessage();
        }
        return "Moved/Renamed: " + source + " -> " + destination;
    }
    private String removeFile(String[] args) {
        if (args.length == 0) {
            return "Usage: rm <file>";
        }
        Path file = currentDirectory.resolve(args[0]);
        try{
            Files.deleteIfExists(file);
        }
        catch (IOException e){
            return ERROR_MESSAGE + e;
        }
        return "Removed: " + file;
    }
    private String displayFileContents(String[] args) {
        if (args.length == 0) {
            return "Usage: cat <file>";
        }
        Path file = currentDirectory.resolve(args[0]);
        try{
            if (Files.exists(file)) {
                return Files.readString(file);
            }
        }
        catch (IOException e){
            return ERROR_MESSAGE + e;
        }
        return "File not found: " + args[0];
    }
}