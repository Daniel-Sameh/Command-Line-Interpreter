package org.os;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Binding;
import org.jline.reader.Reference;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class CLI{
    private static String ERROR_MESSAGE = "An unexpceted error occured: ";
    private final Map<String, Function<String[], String>> commandRegistry = new HashMap<>();
    private Path currentDirectory;
    public CLI(){
        currentDirectory = Paths.get("").toAbsolutePath();
        commandRegistry.put("pwd", args -> currentDirectory.toString());
        commandRegistry.put("cd", this::changeDirectory);
        commandRegistry.put("ls", this::listDirectory);
        commandRegistry.put("mkdir", this::createNewDirectory);
        commandRegistry.put("rmdir", this::removeDirectory);
        commandRegistry.put("touch", this::createNewFile);
        commandRegistry.put("mv", this::moveOrRename);
        commandRegistry.put("rm", this::removeFile);
        commandRegistry.put("cat", this::displayFileContents);
    }
    public String getCurrentDirectory(){
        return currentDirectory.toString();
    }
    public String executeCommand(String command){
        String[] pipelineSeparatedCommand = command.split("\\|");
        String prevInput = "";

        for (int i = 0; i < pipelineSeparatedCommand.length; i++) {
            String pipelineCommand = pipelineSeparatedCommand[i].trim();
            String[] args = pipelineCommand.split("\\s+");
            String cmd = args[0];
            args = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];

            // Execute each command
            prevInput = executeSingleCommand(cmd, args, prevInput);

            // If the next command is "more" or "less", paginate the output
            if (i < pipelineSeparatedCommand.length - 1 &&  (pipelineSeparatedCommand[i + 1].trim().equals("more"))) {
                paginateOutputMore(prevInput);
                return "";
            }else if (i<pipelineSeparatedCommand.length-1 && (pipelineSeparatedCommand[i+1].trim().equals("less"))){
                paginateOutputLess(prevInput);
                return "";
            }
        }
        return prevInput;

    }
    private String executeSingleCommand(String cmd, String[] args, String input) {
        Function<String[], String> commandFunction = commandRegistry.get(cmd);
        if (commandFunction == null) {
            return "\u001B[31mError! Unknown command: \u001B[0m" +"\u001B[33m"+ cmd+"\u001B[0m";
        }

        try {
            return commandFunction.apply(args);
        } catch (Exception e) {
            return "Error executing command: " + cmd + " - " + e.getMessage();
        }
    }
    private void paginateOutputMore(String output) {
        Scanner scanner = new Scanner(System.in);
        String[] lines = output.split("\n");
        int linesPerPage = 10; // Number of lines to display per page
        int currentLine = 0;

        while (currentLine < lines.length) {
            // Display a page of lines
            for (int i = 0; i < linesPerPage && currentLine < lines.length; i++) {
                System.out.println(lines[currentLine]);
                currentLine++;
            }

            // Wait for user input to continue
            if (currentLine < lines.length) {
                System.out.print("\u001B[47m\u001B[30m-- More -- (Press Enter to continue, 'q' to quit): \u001B[0m");
                String input = scanner.nextLine();
                // Clear the "-- More --" line by overwriting with spaces
//                System.out.print("\033[2A");
//                System.out.print("\r"); // Move to the start of the line
//                System.out.print("                                        "); // Print spaces to clear the line
//                System.out.print("\r"); // Move back to the start of the line


//                System.out.print(String.format("\033[%dA",2)); // Move up
//                System.out.print("\033[2K");
//                System.out.print("\033[H\033[2J");
                System.out.flush();
//                System.out.print(String.format("\033[2J"));
                if (input.equalsIgnoreCase("q")) {
                    break;
                }
            }
            ++currentLine;
        }
    }
    private void paginateOutputLess(String output){
        Scanner scanner = new Scanner(System.in);
        String[] lines = output.split("\n");
        int linesPerPage = 10;
        int currentLine = 0;

    }
    private String changeDirectory(String[] args){
        if (args.length == 0){
            return "Usage: cd <directory>";
        }
        if (args[0].charAt(0)=='.'&&args[0].length()==args[0].chars().filter(c -> c == '.').count()&& args[0].length()!=2){
            return "Usage: cd <directory>";
        }
        String path = "";
        int idx=0;
        while (idx<args.length &&args[idx]!="|"){
            path += args[idx]+" ";
            ++idx;
        }
        path = path.trim();
        Path newPath = currentDirectory.resolve(path).normalize();
        if (Files.exists(newPath) && Files.isDirectory(newPath)){
            currentDirectory = newPath;
            return "Directory changed: " + path;
        }
        return "Directory not found: " + path;
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
    private String piplineMore(String file){
        return "Not implemented";
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