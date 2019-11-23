import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.function.Function;

public class LcsBruteForcePerformance {

    static ThreadMXBean bean = ManagementFactory.getThreadMXBean();

    /* define constants */
    static long MAXVALUE = 2000000000;
    static long MINVALUE = -2000000000;
    static int numberOfTrials = 5;      // adjust numberOfTrials and MAXINPUTSIZE based on available
    static int MAXINPUTSIZE = (int) Math.pow(2,12);  // time, processor speed, and available memory
    static int MININPUTSIZE = 1;

    static String ResultsFolderPath = "/home/steve/Results/"; // pathname to results folder
    static FileWriter resultsFile;
    static PrintWriter resultsWriter;

    public static void main(String[] args) {
        // run the whole experiment at least twice, and expect to throw away the data from the earlier runs, before java has fully optimized
        System.out.println("Running first full experiment...");
        runFullExperiment("LcsBruteForce-Exp1-ThrowAway-SameStrings.txt");
        System.out.println("Running second full experiment...");
        runFullExperiment("LcsBruteForce-Exp2-SameStrings.txt");
        System.out.println("Running third full experiment...");
        runFullExperiment("LcsBruteForce-Exp3-SameStrings.txt");

        // verify that the algorithm works
        System.out.println("");
        System.out.println("----Verification Test----");

        System.out.println("The expected outputs are 5, 8, and 5.");
        String S1a = "lmnAAAAAopq";
        String S2a = "abAAAAAAAcd";
        System.out.println("The LCS is length " + LcsBruteForce(S1a,S2a) + ".");
        String S1b = "AAAAAAAA";
        String S2b = "AAAAAAAA";
        System.out.println("The LCS is length " + LcsBruteForce(S1b,S2b) + ".");
        String S1c = "abcdefghAAAAAAAAijklmnopqrstuvwxyz";
        String S2c = "rlstneAAAAA123456";
        System.out.println("The LCS is length " + LcsBruteForce(S1c,S2c) + ".");
    }

    static void runFullExperiment(String resultsFileName) {
        try {
            resultsFile = new FileWriter(ResultsFolderPath + resultsFileName);
            resultsWriter = new PrintWriter(resultsFile);
        } catch (Exception e) {
            System.out.println("*****!!!!!  Had a problem opening the results file " + ResultsFolderPath + resultsFileName);
            return; // not very foolproof... but we do expect to be able to create/open the file...
        }

        ThreadCpuStopWatch BatchStopwatch = new ThreadCpuStopWatch(); // for timing an entire set of trials
        ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trial

        resultsWriter.println("#N      AverageTime"); // # marks a comment in gnuplot data
        resultsWriter.flush();

        /* for each size of input we want to test: in this case starting small and doubling the size each time */
        for (int inputSize = MININPUTSIZE; inputSize <= MAXINPUTSIZE; inputSize *= 2) {
            // progress message...
            System.out.println("Running test for input size " + inputSize + " ... ");

            /* repeat for desired number of trials (for a specific size of input)... */
            long batchElapsedTime = 0;
            // generate a list of random integers in random order to use as test input
            // In this case we're generating one list to use for the entire set of trials (of a given input size)
            //System.out.print("    Generating test data...");
            //long[] testList = createRandomIntegerList(inputSize);
            //System.out.println("...done.");
            //System.out.print("    Running trial batch...");

            /* force garbage collection before each batch of trials run so it is not included in the time */
            System.gc();

            // instead of timing each individual trial, we will time the entire set of trials (for a given input size)
            // and divide by the number of trials -- this reduces the impact of the amount of time it takes to call the
            // stopWatch methods themselves
            //BatchStopwatch.start(); // comment this line if timing trials individually

            // run the trials
            for (long trial = 0; trial < numberOfTrials; trial++) {
                // generate a random list of integers each trial
                //String S1 = randomString(inputSize);
                //String S2 = randomString(inputSize);

                String S1 = repeatedString(inputSize);

                // generate a random key to search in the range of a the min/max numbers in the list
                // long testSearchKey = (long) (0 + Math.random() * (testList[testList.length - 1]));
                /* force garbage collection before each trial run so it is not included in the time */
                // System.gc();

                TrialStopwatch.start(); // *** uncomment this line if timing trials individually
                /* run the function we're testing on the trial input */
                long lengthOfString = LcsBruteForce(S1, S1);
                batchElapsedTime = batchElapsedTime + TrialStopwatch.elapsedTime(); // *** uncomment this line if timing trials individually
            }
            //batchElapsedTime = BatchStopwatch.elapsedTime(); // *** comment this line if timing trials individually
            double averageTimePerTrialInBatch = (double) batchElapsedTime / (double) numberOfTrials; // calculate the average time per trial in this batch

            long N = (long)(Math.floor(Math.log(inputSize)/Math.log(2)));
            /* print data for this size of input */
            resultsWriter.printf("%-12d %-6d %-15.2f \n", inputSize, N, averageTimePerTrialInBatch); // might as well make the columns look nice
            resultsWriter.flush();
            System.out.println(" ....done.");
        }
    }

    // performs the longest common string brute force algorithm shown in class
    public static long LcsBruteForce(String S1, String S2) {
        long l1 = S1.length()-1;    //  length of string 1
        long l2 = S2.length()-1;    // length of string 2
        long lcsLength = 0;         // length of lcs
        long lcsStartIndexInS1;     // start index of lcs in string 1
        long lcsStartIndexInS2;     // start index of lcs in string 2

        for (int i = 0; i <= l1; i++) {
            for (int j = 0; j <= l2; j++) {
                for (int k = 0; k <= Math.min(l1-i, l2-j); k++) {
                    if (S1.charAt(i + k) != S2.charAt(j + k))
                        break;
                    if (k >= lcsLength) {
                        lcsLength = k + 1;
                        lcsStartIndexInS1 = i;
                        lcsStartIndexInS2 = j;
                    }
                }
            }
        }
        return lcsLength;
    }

    // Taken from www.geeksforgeeks.com and modified slightly for this lab.
    // Generate a random alphaNumeric String using Math.random() method.
    public static String randomString(int n)
    {
        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer string
        StringBuilder sb = new StringBuilder(n);
            for (int i = 0; i < n; i++) {
                int index = (int)(AlphaNumericString.length() * Math.random());
                sb.append(AlphaNumericString.charAt(index));
            }
            return sb.toString();
    }

    public static String repeatedString(int n){
        String S1 = "A";
        return S1 + S1.repeat(n);
    }
}
