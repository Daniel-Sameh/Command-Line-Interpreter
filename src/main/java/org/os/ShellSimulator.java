package org.os;
import java.io.IOException;
import java.util.Scanner;

public class ShellSimulator {
    public static void main(String[] args) throws IOException {
        CLI cli = new CLI();
        Scanner scanner = new Scanner(System.in);
        while (true){
            System.out.print(cli.getCurrentDirectory() + "$ ");
            String cmd = scanner.nextLine();
            if (cmd.equals("exit"))
                break;
            System.out.println(cli.executeCommand(cmd));
        }
        scanner.close();
    }
}