package org.os;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

class CLITest {

    //PWD Unit Tests
    @Test
    void pwdCommand() {
        var cli = new CLI();
        String expectedPath = System.getProperty("user.dir");
        assertEquals(cli.executeCommand("pwd"), expectedPath);
    }

    // CD Unit Tests
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

    // LS Unit Tests
    @Test
    void listDriectories(){
        var cli = new CLI();
        var current = cli.getCurrentDirectory();
        assertEquals(cli.executeCommand("ls"), "pom.xml\n" +
                "README.md\n" +
                "src\n" +
                "target\n");

    }

    @Test
    void listDirectoriesWithHiddenFolders(){
        var cli = new CLI();
        var current = cli.getCurrentDirectory();
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
    @Test
    void listDirectoriesHiddenReversed(){
        var cli = new CLI();
        assertEquals(cli.executeCommand("ls -ar"),"target\n" +
                "src\n" +
                "README.md\n" +
                "pom.xml\n" +
                ".idea\n" +
                ".gitignore\n" +
                ".git\n");
    }
    @Test
    void listDirectoriesReversedHidden(){
        var cli = new CLI();
        assertEquals(cli.executeCommand("ls -ra"),"target\n" +
                "src\n" +
                "README.md\n" +
                "pom.xml\n" +
                ".idea\n" +
                ".gitignore\n" +
                ".git\n");
    }

    //mkdir and rmdir Unit Test
    @Test
    void makeAndRemoveDirectoryTest(){
        var cli = new CLI();
        String expectedPath = System.getProperty("user.dir");
        assertEquals(cli.executeCommand("mkdir test"), "Directory created: "+expectedPath+"\\test");
        assertEquals(cli.executeCommand("rmdir test"), "Directory removed: "+expectedPath+"\\test");
    }

    //Touch Unit Test
    @Test
    void makeNewFileTest(){
        var cli = new CLI();
        String expectedPath = System.getProperty("user.dir");
        assertEquals(cli.executeCommand("touch test.txt"),"File created: "+expectedPath+"\\test.txt");
    }

    //remove file Unit Test
    @Test
    void removeFileTest(){
        var cli = new CLI();
        String expectedPath = System.getProperty("user.dir");
        assertEquals(cli.executeCommand("rm test.txt"),"Removed: "+expectedPath+"\\test.txt");
    }

}