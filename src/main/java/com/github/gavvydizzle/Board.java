package com.github.gavvydizzle;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

public class Board {

    private final Position[][] board;
    private boolean solved = false;
    private int count = 0;
    private int solveDepth = 0;

    /**
     * Creates a new sudoku board
     * @param input A 9x9 character grid
     */
    public Board(char[][] input) {
        board = new Position[9][9];

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                char c = input[i][j];
                if (c == '.') {
                    board[i][j] = new Position();
                }
                else {
                    board[i][j] = new Position(c - '0');
                }
            }
        }
    }

    private Board() {
        board = new Position[9][9];
    }

    /**
     * @return A clone of this board
     */
    private Board cloneBoard() {
        Board nb = new Board();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                nb.board[i][j] = board[i][j].clonePosition();
            }
        }
        return nb;
    }

    //*********************************
    //******** Simplification *********
    //*********************************

    /**
     * Performs all initial simplifications of the problem
     */
    public void simplify() {
        removeInvalidAnswers();
        simplifyTrivialPositions();
        simplifySingleOptions();
    }

    /**
     * Removes possible answers from a position by looking at the row/col/grid that it belongs to
     */
    private void removeInvalidAnswers() {
        Set<Integer> set = new HashSet<>(9);

        // Row check
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int ans = board[i][j].getAnswer();
                if (isValidNumber(ans)) {
                    set.add(ans);
                }
            }
            if (set.isEmpty()) continue;

            for (int j = 0; j < 9; j++) {
                int ans = board[i][j].getAnswer();
                if (!isValidNumber(ans)) {
                    board[i][j].removePossibleAnswers(set);
                }
            }
            set.clear();
        }

        // Column check
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int ans = board[j][i].getAnswer();
                if (isValidNumber(ans)) {
                    set.add(ans);
                }
            }
            if (set.isEmpty()) continue;

            for (int j = 0; j < 9; j++) {
                int ans = board[j][i].getAnswer();
                if (!isValidNumber(ans)) {
                    board[j][i].removePossibleAnswers(set);
                }
            }
            set.clear();
        }

        // 3x3 grid check
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        int ans = board[r*3+i][c*3+j].getAnswer();
                        if (isValidNumber(ans)) {
                            set.add(ans);
                        }
                    }
                }
                if (set.isEmpty()) continue;

                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        int ans = board[r*3+i][c*3+j].getAnswer();
                        if (!isValidNumber(ans)) {
                            board[r*3+i][c*3+j].removePossibleAnswers(set);
                        }
                    }
                }
                set.clear();
            }
        }
    }

    /**
     * Attempts to simplify the board by solving positions with only one possible answer remaining.
     * This method will terminate when all cells have two or more possible answers or when a solution is found.
     */
    private void simplifyTrivialPositions() {
        boolean simplified;
        do {
            simplified = false;
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (board[i][j].getPossibleAnswers().size() == 1) {
                        simplified = true;
                        int ans = board[i][j].getPossibleAnswers().get(0);
                        board[i][j].solve(ans);
                        removePossibleAnswer(i, j, ans);
                    }
                }
            }
        } while (simplified);
    }

    /**
     * Attempts to simplify the board by solving positions where a number appears exactly once in a row/col/grid.
     */
    private void simplifySingleOptions() {
        // Keeps track of the frequency of each number
        Map<Integer, Integer> map = new HashMap<>(9);

        boolean simplified;
        do {
            simplified = false;

            // Row check
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    for (int ans : board[i][j].getPossibleAnswers()) {
                        map.put(ans, map.getOrDefault(ans, 0) + 1);
                    }
                }
                if (map.isEmpty()) continue;

                for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                    if (entry.getValue() == 1) { // Locate the position with this value and update it
                        simplified = true;
                        int ans = entry.getKey();

                        for (int j = 0; j < 9; j++) {
                            if (board[i][j].getPossibleAnswers().contains(ans)) {
                                board[i][j].solve(ans);
                                removePossibleAnswer(i, j, ans);
                                break;
                            }
                        }
                    }
                }
                map.clear();
            }

            // Column check
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    for (int ans : board[j][i].getPossibleAnswers()) {
                        map.put(ans, map.getOrDefault(ans, 0) + 1);
                    }
                }
                if (map.isEmpty()) continue;

                for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                    if (entry.getValue() == 1) { // Locate the position with this value and update it
                        simplified = true;
                        int ans = entry.getKey();

                        for (int j = 0; j < 9; j++) {
                            if (board[j][i].getPossibleAnswers().contains(ans)) {
                                board[j][i].solve(ans);
                                removePossibleAnswer(j, i, ans);
                                break;
                            }
                        }
                    }
                }
                map.clear();
            }

            // 3x3 grid check
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 3; j++) {
                            for (int ans : board[r * 3 + i][c * 3 + j].getPossibleAnswers()) {
                                map.put(ans, map.getOrDefault(ans, 0) + 1);
                            }
                        }
                    }
                    if (map.isEmpty()) continue;

                    for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                        if (entry.getValue() == 1) { // Locate the position with this value and update it
                            simplified = true;
                            int ans = entry.getKey();

                            for (int i = 0; i < 3; i++) {
                                for (int j = 0; j < 3; j++) {
                                    if (board[r * 3 + i][c * 3 + j].getPossibleAnswers().contains(ans)) {
                                        board[r * 3 + i][c * 3 + j].solve(ans);
                                        removePossibleAnswer(r * 3 + i, c * 3 + j, ans);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    map.clear();
                }
            }

            // New trivial positions probably exist, deal with them now to
            // open up new possibles for simplification within this method
            simplifyTrivialPositions();

        } while (simplified);
    }

    /**
     * Updates the possible answers of positions in this row/col/grid
     * @param row The row
     * @param col The column
     * @param ans The answer to remove from affected positions
     */
    private void removePossibleAnswer(int row, int col, int ans) {
        // row
        for (int i = 0; i < 9; i++) {
            board[row][i].removePossibleAnswer(ans);
        }

        //col
        for (int i = 0; i < 9; i++) {
            board[i][col].removePossibleAnswer(ans);
        }

        // 3x3 grid
        int r = row/3;
        int c = col/3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[r*3+i][c*3+j].removePossibleAnswer(ans);
            }
        }
    }


    //**********************
    //******** DFS *********
    //**********************

    /**
     * Finds a solution using DFS
     */
    public void solveDFS() {
        dfs(cloneBoard(), 0);
    }

    /**
     * Finds a solution using MRV
     */
    public void solveMRV() {
        mrv(cloneBoard(), 0);
    }

    /**
     * Finds a solution using LCV
     */
    public void solveLCV() {
        lcv(cloneBoard(), 0);
    }

    private void dfs(Board board, int depth) {
        count++;

        if (board.isSolved()) {
            solveDepth = depth;
            solved = true;
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    this.board[i][j].setAnswer(board.board[i][j].getAnswer());
                }
            }
            return;
        }

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (!isValidNumber(board.board[i][j].getAnswer())) {

                    // Check all answers of this position by branching
                    for (int ans : board.board[i][j].getPossibleAnswers()) {
                        if (!solved) {
                            Board nb = board.cloneBoard();
                            nb.board[i][j].solve(ans);
                            nb.removePossibleAnswer(i, j, ans);

                            // Attempt to simplify the new board
                            nb.simplifySingleOptions();

                            // Ignore invalid boards
                            if (nb.containsEmptyPossibilities()) continue;
                            if (nb.containsInvalidAnswers()) continue;

                            dfs(nb, depth+1);
                        }
                    }
                    return;
                }
            }
        }
    }

    private void mrv(Board board, int depth) {
        count++;

        if (board.isSolved()) {
            solveDepth = depth;
            solved = true;
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    this.board[i][j].setAnswer(board.board[i][j].getAnswer());
                }
            }
            return;
        }

        // Determine the position via MRV. If multiple positions have the same number of values, the first one found will be chosen.
        int x = -1;
        int y = -1;
        int min = 10;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (!isValidNumber(board.board[i][j].getAnswer())) {
                    int numAnswers = board.board[i][j].getNumPossibleAnswers();
                    if (numAnswers < min) {
                        min = numAnswers;
                        x = i;
                        y = j;
                    }
                }
            }
        }

        // Check all answers of this position by branching
        for (int ans : board.board[x][y].getPossibleAnswers()) {
            if (!solved) {
                Board nb = board.cloneBoard();
                nb.board[x][y].solve(ans);
                nb.removePossibleAnswer(x, y, ans);

                // Attempt to simplify the new board
                nb.simplifySingleOptions();

                // Ignore invalid boards
                if (nb.containsEmptyPossibilities()) continue;
                if (nb.containsInvalidAnswers()) continue;

                mrv(nb, depth+1);
            }
        }
    }

    private void lcv(Board board, int depth) {
        count++;

        if (board.isSolved()) {
            solveDepth = depth;
            solved = true;
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    this.board[i][j].setAnswer(board.board[i][j].getAnswer());
                }
            }
            return;
        }

        PriorityQueue<LCV_Board> queue = new PriorityQueue<>();
        int x = -1;
        int y = -1;
        boolean found = false;

        // Find the next position to fill in
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (!isValidNumber(board.board[i][j].getAnswer())) {
                    x = i;
                    y = j;
                    found = true;
                    break;
                }
            }
            if (found) break;
        }

        // Order the possibilities for the selected position based on LCV
        for (int ans : board.board[x][y].getPossibleAnswers()) {
            Board nb = board.cloneBoard();
            nb.board[x][y].solve(ans);
            nb.removePossibleAnswer(x, y, ans);

            // Attempt to simplify the new board
            nb.simplifySingleOptions();

            // Ignore invalid boards
            if (nb.containsEmptyPossibilities()) continue;
            if (nb.containsInvalidAnswers()) continue;

            if (nb.isSolved()) {
                lcv(nb, depth+1);
                return;
            }

            // More permutations means it is the lesser constraining value
            queue.add(new LCV_Board(nb, nb.getPermutations()));
        }

        // Recursively call ordered boards
        while (!queue.isEmpty()) {
            lcv(queue.poll().board(), depth+1);
            if (solved) break;
        }
    }

    /**
     * @return If an unsolved position contains an empty set of possibilities
     */
    private boolean containsEmptyPossibilities() {
        for (int i = 8; i >= 0; i--) {
            for (int j = 8; j >= 0; j--) {
                if (!isValidNumber(board[i][j].getAnswer()) && board[i][j].getPossibleAnswers().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return If a board contains multiple of the same answer in a row/col/3x3
     */
    private boolean containsInvalidAnswers() {
        Set<Integer> set = new HashSet<>(9);

        // Row check
        for (int i = 8; i >= 0; i--) {
            for (int j = 8; j >= 0; j--) {
                if (isValidNumber(board[i][j].getAnswer()) && !set.add(board[i][j].getAnswer())) return true;
            }
            set.clear();
        }

        // Column check
        for (int i = 8; i >= 0; i--) {
            for (int j = 8; j >= 0; j--) {
                if (isValidNumber(board[j][i].getAnswer()) && !set.add(board[j][i].getAnswer())) return true;
            }
            set.clear();
        }

        // 3x3 grid check
        for (int r = 2; r >= 0; r--) {
            for (int c = 2; c >= 0; c--) {
                for (int i = 2; i >= 0; i--) {
                    for (int j = 2; j >= 0; j--) {
                        if (isValidNumber(board[r*3+i][c*3+j].getAnswer()) && !set.add(board[r*3+i][c*3+j].getAnswer())) return true;
                    }
                }
                set.clear();
            }
        }

        return false;
    }


    /**
     * Determines if this board has any unsolved positions
     * @return True if any position is blank
     */
    private boolean hasUnsolvedPosition() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (!isValidNumber(board[i][j].getAnswer())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Determines if this board is solved
     * @return True if the board is solved
     */
    public boolean isSolved() {
        if (hasUnsolvedPosition()) return false;

        Set<Integer> set = new HashSet<>(9);

        // Row check
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                set.add(board[i][j].getAnswer());
            }

            if (set.size() != 9) return false;
            set.clear();
        }

        // Column check
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                set.add(board[j][i].getAnswer());
            }

            if (set.size() != 9) return false;
            set.clear();
        }

        // 3x3 grid check
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        set.add(board[r*3+i][c*3+j].getAnswer());
                    }
                }

                if (set.size() != 9) return false;
                set.clear();
            }
        }

        return true;
    }

    private boolean isValidNumber(int n) {
        return n != 0;
    }

    /**
     * @return The number of permutations that exist for this board.
     * The number of possible answers for all positions are multiplied.
     */
    private BigInteger getPermutations() {
        BigInteger total = BigInteger.ONE;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int numAnswers = board[i][j].getNumPossibleAnswers();
                if (numAnswers > 0) {
                    total = total.multiply(BigInteger.valueOf(numAnswers));
                }
            }
        }

        return total;
    }


    /**
     * Outputs the current state of the board.
     * Unsolved positions appear as a period.
     */
    public void outputAnswers(FileWriter fileWriter) throws IOException {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int ans = board[i][j].getAnswer();
                if (isValidNumber(ans)) {
                    output.append(ans).append("  ");
                }
                else {
                    output.append(".");
                }
            }
            if (i < 8) output.append("\n");
        }

        fileWriter.write(output + "\n");
    }

    /**
     * Outputs the possibilities for the board.
     * An empty set (denoted as {}) means that position is solved.
     */
    public void outputPossibilities(FileWriter fileWriter) throws IOException {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int ans = board[i][j].getAnswer();
                if (isValidNumber(ans)) {
                    output.append("{}          ");
                    continue;
                }

                output.append("{");
                for (int k = 1; k <= 9; k++) {
                    if (board[i][j].getPossibleAnswers().contains(k)) {
                        output.append(k);
                    }
                    else {
                        output.append(".");
                    }
                }
                output.append("} ");
            }
            if (i < 8) output.append("\n");
        }

        fileWriter.write(output + "\n");
    }

    /**
     * Outputs the possibilities for the board.
     * An empty set (denoted as {}) means that position is solved.
     */
    public void outputAnswerOrPossibilities() {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int ans = board[i][j].getAnswer();
                if (isValidNumber(ans)) {
                    output.append("{").append(ans).append("}         ");
                    continue;
                }

                output.append("{");
                for (int k = 1; k <= 9; k++) {
                    if (board[i][j].getPossibleAnswers().contains(k)) {
                        output.append(k);
                    }
                    else {
                        output.append(".");
                    }
                }
                output.append("} ");
            }
            if (i < 8) output.append("\n");
        }

        System.out.println(output);
    }

    public int getCount() {
        return count;
    }

    public int getSolveDepth() {
        return solveDepth;
    }

    public boolean solved() {
        return solved;
    }
}
