package org.os;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

class CLITest {
    @Test
    void changeDirectoryBackward(){
        var cli = new CLI();
        String oldPath = cli.getCurrentDirectory();
        var result = cli.executeCommand("cd ..");
        String newPath = cli.getCurrentDirectory();
        var slashes = oldPath.toString().chars().filter(c -> c == '\\').count();
        var newSlashes= newPath.toString().chars().filter(c -> c == '\\').count();
        assertEquals(slashes-newSlashes, 1);
    }

    @Test
    void changeDirectoryToNothing(){
        var cli = new CLI();
        assertEquals(cli.executeCommand("cd"), "Usage: cd <directory>");
    }

    @Test
    void changeDirectoryToDummy(){
        var cli = new CLI();
        assertEquals(cli.executeCommand("cd xyzabc"), "Directory not found: xyzabc");
    }

    @Test
    void listDriectories(){
        var cli= new CLI();
        String command = "dir";
        String currentFiles = "";
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                currentFiles += line + "\n";
            }
            assertEquals(cli.executeCommand("ls"), currentFiles);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void listDirectoriesWithHiddenFolders(){
        var cli = new CLI();
        var current = cli.getCurrentDirectory();
//        File[] files = current.;
        assertEquals(cli.executeCommand("ls -a"), ".git\n" +
                ".gitignore\n" +
                ".idea\n" +
                "pom.xml\n" +
                "README.md\n" +
                "src\n" +
                "target\n");
    }

    @Test
    void listDirectoriesReversed(){
        var cli = new CLI();
        assertEquals(cli.executeCommand("ls -r"),"target\n" +
                "src\n" +
                "README.md\n" +
                "pom.xml\n");
    }

}