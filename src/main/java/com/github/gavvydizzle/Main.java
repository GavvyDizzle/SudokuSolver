package com.github.gavvydizzle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final List<String> algorithmList = List.of("dfs", "mrv", "lcv");
    private static int numBoards = 0;
    private static int solvedBoards = 0;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Missing arguments: input_file and algorithm");
            return;
        }
        if (args.length == 1) {
            System.err.println("Missing argument: algorithm");
            return;
        }
        else if (!args[0].endsWith(".txt")) {
            System.err.println("Only .txt files are accepted");
            return;
        }

        Scanner sc;
        try {
            sc = new Scanner(new File(args[0]));
        } catch (FileNotFoundException e) {
            System.err.println("No file exists with the name: " + args[0]);
            return;
        }

        String alg = args[1].toLowerCase();

        // Special argument to run all algorithm types and print to CSV
        if (alg.equals("__csv")) {
            try {
                FileWriter fileWriter = new FileWriter("output.txt");
                fileWriter.write("board, dfs(ms), mrv(ms), lcv(ms)\n");

                do {
                    outputData(sc, fileWriter);
                    if (sc.hasNext()) sc.nextLine();
                } while (sc.hasNext());

                fileWriter.close();
                sc.close();

                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        if (!algorithmList.contains(alg)) {
            System.err.println("Invalid algorithm: " + args[1]);
            System.err.println("Allowed types: " + algorithmList);
            return;
        }

        // Run with an algorithm
        try {
            FileWriter fileWriter = new FileWriter("output.txt");

            do {
                numBoards++;
                solveFromInput(sc, alg, fileWriter);
                if (sc.hasNext()) sc.nextLine();
            } while (sc.hasNext());

            fileWriter.close();

            System.out.println("Attempted to solve " + numBoards + " board(s)...");
            if (numBoards == solvedBoards) {
                System.out.println("Solved all " + solvedBoards + " board(s)!");
            }
            else {
                System.out.println("Solved " + solvedBoards + " board(s)");
                System.out.println("Failed to solve " + (numBoards - solvedBoards) + " board(s)");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        sc.close();
    }

    public static void solveFromInput(Scanner sc, String algorithm, FileWriter fileWriter) throws IOException {
        String name = sc.nextLine();
        char[][] input = new char[9][9];

        for (int i = 0; i < 9; i++) {
            char[] arr = sc.nextLine().toCharArray();
            input[i] = arr;
        }

        fileWriter.write("Board: " + name + "\n");
        Board board = new Board(input);

        long preSolveTime = System.currentTimeMillis();
        board.simplify();

        switch (algorithm) {
            case "dfs" -> board.solveDFS();
            case "mrv" -> board.solveMRV();
            case "lcv" -> board.solveLCV();
        }

        long postSolveTime = System.currentTimeMillis();

        if (board.solved()) {
            fileWriter.write(algorithm.toUpperCase() + ": Iterations=" + board.getCount() + " Depth=" + board.getSolveDepth() + "\n");
        }
        else {

            fileWriter.write(algorithm.toUpperCase() + ": Iterations=" + board.getCount() + "\n");
        }
        fileWriter.write("Computation Time: " + (postSolveTime-preSolveTime) + "ms\n");

        if (board.isSolved()) {
            solvedBoards++;
            fileWriter.write("===(SOLVED)===\n");
            board.outputAnswers(fileWriter);
        }
        else {
            fileWriter.write("===(UNSOLVED)===\n");
            board.outputPossibilities(fileWriter);
        }

        fileWriter.write("---------------------------\n");
    }

    public static void outputData(Scanner sc, FileWriter fileWriter) throws IOException {
        String name = sc.nextLine();
        char[][] input = new char[9][9];

        for (int i = 0; i < 9; i++) {
            char[] arr = sc.nextLine().toCharArray();
            input[i] = arr;
        }

        fileWriter.write(name);

        for (String alg : algorithmList) {
            fileWriter.write(",");

            Board board = new Board(input);

            long preSolveTime = System.nanoTime();
            board.simplify();

            switch (alg) {
                case "dfs" -> board.solveDFS();
                case "mrv" -> board.solveMRV();
                case "lcv" -> board.solveLCV();
            }

            long postSolveTime = System.nanoTime();

            fileWriter.write("" + round((postSolveTime - preSolveTime)/1e6, 2));
        }
        fileWriter.write("\n");
    }

    /**
     * Rounds a number to the specified number of decimal places
     *
     * @param value The value to round
     * @param places The maximum number of decimal places
     * @return The rounded number
     */
    public static double round(double value, int places) {
        return new BigDecimal(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }
}