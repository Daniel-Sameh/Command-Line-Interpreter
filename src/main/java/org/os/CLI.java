package org.os;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.BiFunction;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CLI{
    private static String ERROR_MESSAGE = "An unexpceted error occured";
    private final Map<String, Function<String[], String>> commandRegistry = new HashMap<>();
    private final Map<String, String> commandDescription = new HashMap<>();
    private final Map<String, BiFunction<String, String, String>> pipelineFilterRegistry = new HashMap<>();
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
        commandRegistry.put("help", this::helpDisplay);

        commandDescription.put("pwd", "Usage: pwd\n     Displays the current working directory.");
        commandDescription.put("cd", "Usage: cd <directory>\n     Changes the current directory to <directory>.");
        commandDescription.put("ls", "Usage: ls\n     Lists all files and directories in the current directory.");
        commandDescription.put("mkdir", "Usage: mkdir <directory>\n     Creates a new directory named <directory>.");
        commandDescription.put("rmdir", "Usage: rmdir <directory>\n     Removes the specified directory if it is empty.");
        commandDescription.put("touch", "Usage: touch <filename>\n     Creates a new empty file named <filename>.");
        commandDescription.put("mv", "Usage: mv <source> <destination>\n     Moves or renames a file or directory.");
        commandDescription.put("rm", "Usage: rm <filename>\n     Deletes the specified file.");
        commandDescription.put("cat", "Usage: cat <filename>\n     Displays the contents of the specified file.");
        commandDescription.put("help", "Usage: help\n     Displays this help message with a list of available commands.");


        pipelineFilterRegistry.put("less", this::paginateOutputLess);
        pipelineFilterRegistry.put("more", this::paginateOutputMore);
        pipelineFilterRegistry.put("uniq", this::getUniqe);
        pipelineFilterRegistry.put("grep", this::filterWithPattern);
    }

    private String helpDisplay(String[] strings) {
        if (strings.length>0){
            return decorateErrorMessage("","Too many arguments");
        }
        // Build the help message with available commands
        StringBuilder helpMessage = new StringBuilder("Available commands:\n");
        for (String command : commandRegistry.keySet()) {
            helpMessage.append("- ").append(command).append("\n     ").append(commandDescription.get(command)).append("\n");
        }

        return helpMessage.toString();
    }

    // Utillity Functions
    private static void removeLastPrintedLine(){
        System.out.print("\033[1A");
        System.out.print("\033[2K");
        System.out.flush();
    }
    public String getCurrentDirectory(){
        return currentDirectory.toString();
    }
    private String writeToFile(String fileName, String content, Boolean append){
        try{
            String filePath = Paths.get(getCurrentDirectory(), fileName).toString();
            FileWriter writer = new FileWriter(filePath, append);
            writer.write(content);
            writer.close();
        }
        catch (IOException e){
            return decorateErrorMessage("Error writing to", fileName);
        }
        return "";
    }
    private static String decorateErrorMessage(String firstPart, String secondPart){
        return "\u001B[31mError! " + firstPart + ": \u001B[0m" +"\u001B[33m"+ secondPart +"\u001B[0m";
    }

    public String executeCommand(String commands){
        String[] tokens = commands.split("\\s+");
        ArrayList<String> modifiedTokens = new ArrayList<>();
        modifiedTokens.add("");
        for (String token : tokens) {
            if (token.equals("|") || token.equals(">") || token.equals(">>")) {
                modifiedTokens.add(token);
                modifiedTokens.add("");
            } else {
                // Access the last element and remove it
                String cur = modifiedTokens.get(modifiedTokens.size() - 1) + " " + token;
                modifiedTokens.remove(modifiedTokens.size() - 1);
                modifiedTokens.add(cur);
            }
        }
        String prevOutput = executeSingleCommand(modifiedTokens.get(0).trim().split("\\s+"));
        for (int i = 1; i < modifiedTokens.size(); i += 2){
            String token = modifiedTokens.get(i).trim();
            String right = (i + 1 < modifiedTokens.size() ? modifiedTokens.get(i + 1) : "");
            right = right.trim();
            if (token.equals("|")){
                prevOutput = applyPipelineFilter(prevOutput, right);
            } else if (token.equals(">")){
                prevOutput = writeToFile(right, prevOutput, false);
            } else if (token.equals(">>")){
                prevOutput = writeToFile(right, prevOutput, true);
            }else{
                return decorateErrorMessage("Unknown Command/s", token);
            }
        }
        return prevOutput;
    }
    private String executeSingleCommand(String[] args) {
        String cmd = args[0].toLowerCase().trim();
        args = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
        Function<String[], String> commandFunction = commandRegistry.get(cmd);
        if (commandFunction == null) {
            return decorateErrorMessage("Unknown command", cmd);
        }

        try {
            return commandFunction.apply(args);
        } catch (Exception e) {
            return decorateErrorMessage("Error executing command '" + cmd + "'", e.getMessage());
        }
    }
    private String applyPipelineFilter(String prevOutput, String filter){
        var filteredFilter= filter.split("\\s+");
        BiFunction<String, String, String> pipileFunction = pipelineFilterRegistry.get(filteredFilter[0]);
        if (pipileFunction == null) {
            return decorateErrorMessage("Unknown filter", filter);
        }
        String input="";
        if (filteredFilter[0].equals("grep")){
            for (int i=1; i<filteredFilter.length; ++i){
                input += filteredFilter[i]+" ";
            }
        }
        try {
            return pipileFunction.apply(prevOutput,input);
        } catch (Exception e) {
            return decorateErrorMessage("Error applying filter '" + filter + "'", e.getMessage());
        }
    };
    
    // command | more
    private String paginateOutputMore(String output, String dummyInput) {
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
                removeLastPrintedLine();
                if (input.equalsIgnoreCase("q")) {
                    break;
                }
            }
            ++currentLine;
        }
//        scanner.close();
        return "";
    }
    // command | less
    private String paginateOutputLess(String output, String dummyInput){
        Scanner scanner = new Scanner(System.in);
        String[] lines = output.split("\n");
        int linesPerPage = (lines.length < 10 ? lines.length : 10);
        int currentLine = linesPerPage;
        for (int i = 0; i < linesPerPage; i++)
            System.out.println(lines[i]);
        while (currentLine < lines.length){
            String input = scanner.nextLine();
            removeLastPrintedLine();
            if (input.equalsIgnoreCase("q")) {
                break;
            }
            if (input.equals("w")){
                if (currentLine > 0) {
                    removeLastPrintedLine();
                    removeLastPrintedLine();
                    currentLine--;
                }
            }
            else if (input.equals("s")){
                System.out.println(lines[currentLine++]);
            }
        }
//        scanner.close();
        return "";
    }
    // command | uniq
    private String getUniqe(String output, String input){
        Set<String> distinctValues = new HashSet<>(Arrays.asList(output.split("\n")));
        String result = "";
        for (String s: distinctValues){
            result += s+"\n";
        }
        return result;
    }
    // command | grep "pattern"
    private String filterWithPattern(String input, String pattern){
        String[] lines = input.split("\\n");
        StringBuilder filteredLines = new StringBuilder();
        pattern = Pattern.quote(pattern.trim());
        Pattern regexPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);

        for (String line : lines) {
            Matcher matcher = regexPattern.matcher(line);
            if (matcher.find()) { // Checks if the pattern exists in the line
                filteredLines.append(line).append("\n");
            }
        }
        return filteredLines.toString();
    }
    // cd
    private String changeDirectory(String[] args){

        if (
            args.length != 1
            || args[0].charAt(0) == '.' 
            && args[0].length() == args[0].chars().filter(c -> c == '.').count() 
            && args[0].length() != 2
        ){
            return decorateErrorMessage("Usage", "cd <directory>");
        }
        Path newPath = currentDirectory.resolve(args[0]).normalize();
        if (Files.exists(newPath) && Files.isDirectory(newPath)){
            currentDirectory = newPath;
            return "Directory changed: " + args[0];
        }
        return decorateErrorMessage("Directory not found", args[0]);
    }
    // ls
    private String listDirectory(String[] args){
        Boolean hidden = false, reversed = false;
        for (String arg: args){
            if (arg.equals("-a"))
                hidden = true;
            else if (arg.equals("-r"))
                reversed = true;
            else 
                return decorateErrorMessage("Usage", "ls [-a] [-r]");
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
            return decorateErrorMessage(ERROR_MESSAGE, e.getMessage());
        }
    }
    // mkdir
    private String createNewDirectory(String[] args){
        if (args.length != 1) {
            return decorateErrorMessage("Usage", "mkdir <directory>");
        }
        Path newDir = currentDirectory.resolve(args[0]);
        try{
            Files.createDirectories(newDir);
        }
        catch(FileAlreadyExistsException e){
            return decorateErrorMessage("File already exists", newDir.toString());
        }
        catch(IOException e){
            return ERROR_MESSAGE + e;
        }
        return "Directory created: " + newDir;
    }
    // rmdir
    private String removeDirectory(String[] args) {
        if (args.length != 1) {
            return decorateErrorMessage("Usage", "rmdir <directory>");
        }
        Path dir = currentDirectory.resolve(args[0]);
        if (!Files.exists(dir) || !Files.isDirectory(dir)){
            return decorateErrorMessage("Directory not found", dir.toString());
        }
        try{
            Files.deleteIfExists(dir);
        }
        catch (IOException e){
            return ERROR_MESSAGE + e;
        }
        return "Directory removed: " + dir;
    }
    // touch
    private String createNewFile(String[] args){
        if (args.length != 1) {
            return decorateErrorMessage("Usage", "touch <file>");
        }
        Path file = currentDirectory.resolve(args[0]);
        try{
            Files.createFile(file);
        }
        catch (FileAlreadyExistsException e){
            return decorateErrorMessage("File already exists", file.toString());
        }
        catch (IOException e){
            return ERROR_MESSAGE + e;
        }
        return "File created: " + file;
    }
    // mv
    private String moveOrRename(String[] args){
        if (args.length != 2) {
            return decorateErrorMessage("Usage", "mv <source> <destination>");
        }
        Path source = currentDirectory.resolve(args[0]);
        Path destination = currentDirectory.resolve(args[1]);
        if (!Files.exists(source)){
            return decorateErrorMessage("Source not found", source.toString());
        }
        if (Files.isDirectory(destination)) {
            destination = destination.resolve(source.getFileName());
        }
        try {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return decorateErrorMessage("Error moving/renaming", e.getMessage());
        }
        return "Moved/Renamed: " + source + " -> " + destination;
    }
    // rm
    private String removeFile(String[] args) {
        if (args.length != 1) {
            return decorateErrorMessage("Usage", "rm <file>");
        }
        Path file = currentDirectory.resolve(args[0]);
        if (!Files.exists(file)){
            return decorateErrorMessage("File not found", file.toString());
        }
        try{
            Files.deleteIfExists(file);
        }
        catch (IOException e){
            return decorateErrorMessage("Error removing file/directory", e.getMessage());
        }
        return "Removed: " + file;
    }
    // cat
    private String displayFileContents(String[] args) {
        if (args.length != 1) {
            return decorateErrorMessage("Usage", "cat <file>");
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
        return decorateErrorMessage("File not found", file.toString());
    }
}