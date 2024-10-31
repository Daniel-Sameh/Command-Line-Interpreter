package org.os;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.*;
import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

class CLITest {

    //Wrong Command unit test
    @Test
    void wrongCommand(){
        var cli = new CLI();
        assertEquals(cli.executeCommand("xyz"),"\u001B[31mError! Unknown command: \u001B[0m\u001B[33mxyz\u001B[0m");
    }
    // Empty Pipline argument
    @Test
    void emptyPiplineArgument(){
        var cli = new CLI();
        assertEquals(cli.executeCommand("ls |"),"\u001B[31mError! Unknown filter: \u001B[0m\u001B[33m\u001B[0m");
    }
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
        assertEquals(cli.executeCommand("cd"), "\u001B[31mError! Usage: \u001B[0m\u001B[33mcd <directory>\u001B[0m");
    }

    @Test
    void changeDirectoryToDummy(){
        var cli = new CLI();
        assertEquals(cli.executeCommand("cd xyzabc"), "\u001B[31mError! Directory not found: \u001B[0m\u001B[33mxyzabc\u001B[0m");
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
    @Test
    void wrongLSArguments(){
        var cli = new CLI();
        assertEquals(cli.executeCommand("ls ? abc xyz"),"[31mError! Unknown Commands for `ls`: [0m[33m?, abc, xyz[0m");
    }

    //mkdir and rmdir Unit Test
    @Test
    void makeAndRemoveDirectoryTest(){
        var cli = new CLI();
        String expectedPath = System.getProperty("user.dir");
        assertEquals(cli.executeCommand("mkdir test"), "Directory created: "+expectedPath+"\\test");
        assertEquals(cli.executeCommand("rmdir test"), "Directory removed: "+expectedPath+"\\test");
    }

    //Touch and RM Unit Test
    @Test
    void makeAndDeleteNewFileTest(){
        var cli = new CLI();
        String expectedPath = System.getProperty("user.dir");
        assertEquals(cli.executeCommand("touch test.txt"),"File created: "+expectedPath+"\\test.txt");
        assertEquals(cli.executeCommand("rm test.txt"),"Removed: D:\\FCAI materials\\Level 3 Term 1\\Operating Systems\\Assignments\\Assignment#1\\CLI\\test.txt");
    }

    // cat with grep project (cat pom.xml | grep project)
    @Test
    void catWithGrep(){
        var cli = new CLI();
        assertEquals(cli.executeCommand("ls | grep ."),"pom.xml\n" +
                "README.md\n");
    }

    // more and less pipline unit tests
    @Test
    void moreAndLessInPipline(){
        var cli = new CLI();
        assertEquals(cli.executeCommand("ls | more"), "");
        assertEquals(cli.executeCommand("ls | less"), "");
    }

    //Uniq filter unit test
    @Test
    void uniquePiplineFilter(){
        var cli = new CLI();
        assertEquals(cli.executeCommand("ls | uniq"), "src\n" +
                "pom.xml\n" +
                "README.md\n" +
                "target\n");
    }

    // > operator and file generation
    @Test
    void writeOperatorTest(){
        var cli = new CLI();
        String expectedOutput = cli.executeCommand("ls");
        cli.executeCommand("ls > test.txt");
        String currentDirectory = cli.getCurrentDirectory();
        Path filePath = Paths.get(currentDirectory, "test.txt");
        assertTrue(Files.exists(filePath), "test.txt should exist after executing the command");

        try {

            String actualOutput = Files.readString(filePath);
            // Assert the content of test.txt is as expected
            assertEquals(expectedOutput.trim(), actualOutput.trim(), "The contents of test.txt should match the output of `ls`");
            // deleting the test file
            cli.executeCommand("rm test.txt");

        } catch (IOException e) {
            fail("IOException occurred while reading test.txt: " + e.getMessage());
        }
    }

    // >> operator test
    @Test
    void writeAndAppendToFileTest(){
        var cli = new CLI();
        String expectedOutput = cli.executeCommand("ls");
        cli.executeCommand("ls >> test.txt");
        expectedOutput+= cli.executeCommand("ls");
        cli.executeCommand("ls >> test.txt"); //Append to test.txt
        String currentDirectory = cli.getCurrentDirectory();
        Path filePath = Paths.get(currentDirectory, "test.txt");
        assertTrue(Files.exists(filePath), "test.txt should exist after executing the command");
        try {

            String actualOutput = Files.readString(filePath);
            // Assert the content of test.txt is as expected
            assertEquals(expectedOutput.trim(), actualOutput.trim(), "The contents of test.txt should match the output of `ls`");
            // deleting the test file
            cli.executeCommand("rm test.txt");

        } catch (IOException e) {
            fail("IOException occurred while reading test.txt: " + e.getMessage());
        }
    }

    //MV file to directory
    @Test
    void moveTestTxtToTestDirectory(){
        var cli = new CLI();
        assertEquals(cli.executeCommand("mv test.txt"),"\u001B[31mError! Usage: \u001B[0m\u001B[33mmv <source> <destination>\u001B[0m");
        assertEquals(cli.executeCommand("mv test.txt test xyz"),"\u001B[31mError! Usage: \u001B[0m\u001B[33mmv <source> <destination>\u001B[0m");
        cli.executeCommand("cd ..");
        cli.executeCommand("cd ..");
        cli.executeCommand("mkdir test");
        cli.executeCommand("ls > test.txt");
        assertEquals(cli.executeCommand("mv test.txt test"), "Moved/Renamed: D:\\FCAI materials\\Level 3 Term 1\\Operating Systems\\Assignments\\test.txt -> D:\\FCAI materials\\Level 3 Term 1\\Operating Systems\\Assignments\\test\\test.txt");
        cli.executeCommand("cd test");
        cli.executeCommand("rm test.txt");
        cli.executeCommand("cd ..");
        assertEquals(cli.executeCommand("rmdir test"), "Directory removed: D:\\FCAI materials\\Level 3 Term 1\\Operating Systems\\Assignments\\test");
    }

    //Cat wrong file name
    @Test
    void catWrongFile(){
        var cli = new CLI();
        assertEquals(cli.executeCommand("cat xyz"),"\u001B[31mError! File not found: \u001B[0m\u001B[33mD:\\FCAI materials\\Level 3 Term 1\\Operating Systems\\Assignments\\Assignment#1\\CLI\\xyz\u001B[0m");
        String expectedOutput = cli.executeCommand("ls");
        cli.executeCommand("ls > test.txt");
        assertEquals(cli.executeCommand("cat test.txt"),expectedOutput);
        cli.executeCommand("rm test.txt");
    }

    //The following need real interaction with the user to test it...
//    @Test
//    void bigPiplineMoreTest(){
//        var cli = new CLI();
//        assertEquals(cli.executeCommand("cat pom.xml | more"), "");
//        assertEquals(cli.executeCommand("cat pom.xml | less"), "");
//    }


}