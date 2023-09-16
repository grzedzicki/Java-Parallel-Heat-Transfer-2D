package org.example;

import org.pcj.PCJ;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

import static java.lang.Math.sqrt;

public class FileOperations {
    static void setNodes(int numberOfNodes) throws IOException {
        FileWriter myWriter = new FileWriter("nodes.txt");
        for(int i = 0; i < numberOfNodes; i++) {
            myWriter.write("localhost\n");
        }
        myWriter.close();
    }

    static void printArray(double [][] array, int sizeX, int sizeY){
        for (int i=0; i<sizeX;i++)
            for(int j=0; j<sizeY;j++)
                System.out.println(PCJ.myId() + ": " + array[i][j]);
    }
    static double[][] loadTemps(int threadId, String filename, int sizeX) throws IOException {
        int filelength = (int) sqrt(sizeX);
        int filelengthdown = filelength/ PCJ.threadCount();
        int rest = filelength%PCJ.threadCount();
        Scanner sc = new Scanner(new BufferedReader(new FileReader(filename)));
        double [][] myArray;
        if(PCJ.myId() == PCJ.threadCount()-1)
             myArray = new double[filelengthdown+rest][filelength];
        else
            myArray = new double[filelengthdown][filelength];

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int i = 0, j = 0, counter = 0;
            int threadLine;

            if(PCJ.threadCount() == 1){
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(" ");
                    for (int p = 0; p < filelength; p++) {
                        myArray[i][j] = Integer.parseInt(parts[p]);
                        j++;
                    }
                    counter++;
                    i++;
                    j = 0;
                }
                return myArray;
            }

            if (PCJ.myId() == 0)
                threadLine = filelengthdown * PCJ.myId();
            else threadLine = filelengthdown * PCJ.myId();
            if (PCJ.myId() == 0) {
                while ((line = br.readLine()) != null) {
                    if (counter == filelengthdown) break;
                    String[] parts = line.split(" ");
                    for (int p = 0; p < filelength; p++) {
                        myArray[i][j] = Integer.parseInt(parts[p]);
                        j++;
                    }
                    counter++;
                    i++;
                    j = 0;
                }
            }
            if(PCJ.myId()==PCJ.threadCount()-1){
                counter = -1;
                while ((line = br.readLine()) != null) {
                    counter++;
                    if (counter < threadLine) continue;
                    if(counter == filelengthdown*PCJ.myId()+filelengthdown+rest) break;
                    String[] parts = line.split(" ");
                    for (int p = 0; p < filelength; p++) {
                        myArray[i][j] = Integer.parseInt(parts[p]);
                        j++;
                    }
                    i++;
                    j = 0;
                }
            }
            else{
                counter = -1;
                while ((line = br.readLine()) != null) {
                    if(PCJ.myId()==0) break;
                    counter++;
                    if (i == (filelengthdown*PCJ.myId())) break;
                    if (counter < threadLine) continue;
                    if(counter == filelengthdown*PCJ.myId()+filelengthdown) break;
                    String[] parts = line.split(" ");
                    for (int p = 0; p < filelength; p++) {
                        myArray[i][j] = Integer.parseInt(parts[p]);
                        j++;
                    }
                    i++;
                    j = 0;
                }
            }
            } catch(IOException e){
                throw new RuntimeException(e);
            }
        return myArray;
    }

    public void saveToFile1Thread(double[][] array) throws IOException {
        FileWriter myWriter = new FileWriter("output.txt");
        myWriter.write(Arrays.deepToString(array));
        myWriter.close();
    }

    public void saveToFileManyThreads(double[][] array, boolean option) throws IOException {
        FileWriter myWriter;
        if(option)  myWriter = new FileWriter("output.txt");
        else myWriter = new FileWriter("output.txt", true);
        myWriter.write(Arrays.deepToString(array));
        myWriter.close();
    }
}
