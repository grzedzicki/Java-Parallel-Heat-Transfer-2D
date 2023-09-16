package org.example;
import java.io.File;
import java.io.IOException;
import org.pcj.*;

import static java.lang.System.exit;

public class Main{
    static File nodes = new File("nodes.txt");
    private static String[] savedArgs;
    public static String[] getArgs() {
        return savedArgs;
    }
    public static void main(String[] args) throws IOException {
        if(args.length != 5) {
            System.out.println("Incorrect arguments number");
            System.out.println("Correct format: java Main.java NumberOfNodes");
            exit(0);
        }
        savedArgs = args;
        FileOperations FileOps = new FileOperations();
        FileOps.setNodes(Integer.parseInt(args[0]));
        PCJ.executionBuilder (PCJMain.class)
                .addNodes(nodes)
                .start();
    }
}