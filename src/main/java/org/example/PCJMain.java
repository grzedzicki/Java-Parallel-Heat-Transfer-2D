package org.example;

import org.pcj.*;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import static java.lang.Math.sqrt;

@RegisterStorage(PCJMain.Shared.class)
public class PCJMain implements StartPoint {

    @Storage(PCJMain.class)
    enum Shared { myArray, te2, saveArrayUpAndMid, saveArrayLast }
    public double a;

    double alpha = 2.0;
    int delta_x = 1;
    double delta_t = (delta_x^ 2)/(4 * alpha);
    double gamma = (alpha * delta_t) / (delta_x^ 2);
    double [][] myArray = new double[4][4];
    int fileSize = (int)sqrt((Integer.parseInt(returnArgs(1))));
    int filelength = (int) sqrt(Integer.parseInt(returnArgs(1)));
    int filelengthdown = filelength/ PCJ.threadCount();
    int rest = filelength%PCJ.threadCount();

    double[][] newArray1D;
    long timeElapsed = 0;
    long te2 = 0;

    double[][] newArrayPCJ;

    double[][] saveArrayUpAndMid = new double[filelengthdown][filelength];
    double[][] saveArrayLast = new double[filelengthdown+rest][filelength];

    public void main() throws Throwable {
        FileOperations FileOp = new FileOperations();
        myArray = FileOp.loadTemps(PCJ.myId(), returnArgs(3), Integer.parseInt(returnArgs(1)));
        newArray1D = copy(myArray);
        double[][] saveArray = new double[filelength][filelength];
        int loop = Integer.parseInt(returnArgs(2));
        newArrayPCJ = copy(myArray);
        PCJ.barrier();
        long startTime = System.currentTimeMillis();
        for(int k=0; k < loop; k++) {
            PCJ.barrier();
            if(PCJ.threadCount() == 1){
                saveArray = calculateNextStep1Thread();
                myArray = copy(newArray1D);
            } else if (PCJ.myId() < PCJ.threadCount() - 1) {
                saveArrayUpAndMid = calculateNextStep();
            }
            else if(PCJ.myId() == PCJ.threadCount() - 1){
                saveArrayLast = calculateNextStep();
            }
            if(PCJ.threadCount() != 1) {
                PCJ.barrier();
                myArray = copy(newArrayPCJ);
            }
        }
        long endTime = System.currentTimeMillis();
        te2 = endTime - startTime;
        long duration = endTime - startTime;
        if(PCJ.threadCount() == 1)
            System.out.println("Time elapsed : " + (duration) + "ms");
        if(PCJ.threadCount() == 1) {
            FileOp.saveToFile1Thread(saveArray);
            return;
        }
        PCJ.barrier();
        if(PCJ.myId() == 0)
        {
            long av = 0;
            for(int i=0; i < PCJ.threadCount(); i++){
                av += (long) PCJ.get(i,Shared.te2);
            }
            System.out.println("Average time: " + av/ PCJ.threadCount() + "ms");
        }

        if(PCJ.myId() == 0){
            FileOp.saveToFileManyThreads(saveArrayUpAndMid,true);
            for(int i = 1; i < PCJ.threadCount()-1; i++){
                saveArrayUpAndMid = PCJ.get(i, Shared.saveArrayUpAndMid);
                FileOp.saveToFileManyThreads(saveArrayUpAndMid,false);
            }
            saveArrayLast = PCJ.get(PCJ.threadCount()-1,Shared.saveArrayLast);
            FileOp.saveToFileManyThreads(saveArrayLast,false);
        }
    }

    public String returnArgs(int value) {
        String[] args = Main.getArgs();
        return args[value];
    }

    public static double[][] copy(double[][] src) {
        if (src == null) {
            return null;
        }

        double[][] copy = new double[src.length][];
        for (int i = 0; i < src.length; i++) {
            copy[i] = Arrays.copyOf(src[i], src[i].length);
        }

        return copy;
    }

    public double[][] calculateNextStep1Thread() throws IOException {
        for (int i = 1; i < fileSize - 1; i += delta_x)
            for (int j = 1; j < fileSize - 1; j+= delta_x) {
                    newArray1D[i][j] =  (gamma * (myArray[i + 1][j] + myArray[i - 1][j] + myArray[i][j + 1] + myArray[i][j - 1] - 4 * myArray[i][j])) + myArray[i][j];
            }
        return newArray1D;
    }

    public double[][] calculateNextStep(){
        double[] hArray = new double[filelength];
        if(PCJ.myId() == 0){
                for (int i = 1; i < filelengthdown; i += delta_x) {
                    if (i == filelengthdown - 1) {hArray = PCJ.get(PCJ.myId() + 1, Shared.myArray, 0);
                    }
                    for (int j = 1; j < filelength - 1; j += delta_x)
                    {
                        if (i == filelengthdown - 1)
                            newArrayPCJ[i][j] = (gamma * (hArray[j] + myArray[i - 1][j] + myArray[i][j + 1] + myArray[i][j - 1] - 4 * myArray[i][j])) + myArray[i][j];
                        else newArrayPCJ[i][j] = (gamma * (myArray[i+1][j] + myArray[i - 1][j] + myArray[i][j + 1] + myArray[i][j - 1] - 4 * myArray[i][j])) + myArray[i][j];
                    }
                }
            return newArrayPCJ;
        }

        if(PCJ.myId() == PCJ.threadCount()-1){
                for(int i = 0; i < filelengthdown + rest - 1; i += delta_x) {
                    if (i == 0) hArray = PCJ.get(PCJ.myId() - 1, Shared.myArray, filelengthdown - 1);
                    for (int j = 1; j < filelength - 1; j += delta_x) {
                        if(i == 0) newArrayPCJ[i][j] = (gamma * (myArray[i + 1][j] + hArray[j] + myArray[i][j + 1] + myArray[i][j - 1] - 4 * myArray[i][j])) + myArray[i][j];
                        else newArrayPCJ[i][j] = (gamma * (myArray[i + 1][j] + myArray[i - 1][j] + myArray[i][j + 1] + myArray[i][j - 1] - 4 * myArray[i][j])) + myArray[i][j];
                    }
                }
            return newArrayPCJ;
        }

        else{
                for(int i = 0; i < filelengthdown; i += delta_x) {
                    if(i == 0){
                        hArray = PCJ.get(PCJ.myId() - 1, Shared.myArray, filelengthdown - 1);
                    } else if (i == filelengthdown - 1) {
                        hArray = PCJ.get(PCJ.myId() + 1, Shared.myArray, 0);
                    }
                    for (int j = 1; j < filelength - 1; j += delta_x) {
                        if (i==0) newArrayPCJ[i][j] = (gamma * (myArray[i+1][j] + hArray[j] + myArray[i][j + 1] + myArray[i][j - 1] - 4 * myArray[i][j])) + myArray[i][j];
                        else if (i==filelengthdown-1) newArrayPCJ[i][j] = (gamma * (hArray[j] + myArray[i-1][j] + myArray[i][j + 1] + myArray[i][j - 1] - 4 * myArray[i][j])) + myArray[i][j];
                        else newArrayPCJ[i][j] = (gamma * (myArray[i-1][j] + myArray[i+1][j] + myArray[i][j + 1] + myArray[i][j - 1] - 4 * myArray[i][j])) + myArray[i][j];
                    }
                }
            return newArrayPCJ;
        }
    }
}
