# Sudoku Solver
An AI homework assignment

## Program Design
The program allows for three different algorithms to be used to solve sudoku boards provided in the input file
- The program starts by identifying the provided input file and solver algorithm
- The program parses boards from the input file and solves them
- All three algorithms use recursion to find a solution
- After solving a board (or not), the program will print the solution(s) to the output file

## User Manual
The following subsections describe how to compile, run, and interact with the program

### Compiling
- Make sure you have Java and Maven on your system before compiling
- Navigate to the top level directory. The `src` folder should be visible
- Run `mvn package` to compile the .jar file to the `/target` directory
- Navigate to the `/target` directory to run the program

### Running
- Run with `java -jar SudokuSolver-1.0.0.jar [input_file] [algorithm]`
- Where the `input_file` is the name of the text file to read from
  - Make sure your input files are in the `/target` directory
- And the `algorithm` is how the board(s) will be solved: `dfs` `mrv` `lcv`
    - Using `__csv` for the algorithm tells the program to output runtime data to the output file

### Input File
Input files must be a .txt file. Sudoku boards follow the format:
```
easy1
5.6918.23
7136.2.49
.8.....51
6.4.....2
.2786....
.....41..
3..14.2..
...5.63.8
.7......6
```
Each board needs a name followed by 9 lines of length 9 with numbers [1,9] and periods to signify unknown spaces.
To add more boards to the input file, put a blank line between successive boards.

### Output File
The output file contains information about the algorithm you chose to run.
- This file is `output.txt` and will exist in the `/target` directory after running the program
- The output will be formatted in the following way:
```
Board: {board}
{alg}: Iterations={iter} Depth={depth}
Computation Time: {ms}ms
===(SOLVED)===
5  4  6  9  1  8  7  2  3  
7  1  3  6  5  2  8  4  9  
9  8  2  4  7  3  6  5  1  
6  3  4  7  9  1  5  8  2  
1  2  7  8  6  5  9  3  4  
8  5  9  2  3  4  1  6  7  
3  6  8  1  4  7  2  9  5  
4  9  1  5  2  6  3  7  8  
2  7  5  3  8  9  4  1  6  
```
Output placeholders:
- `{board}` The name of the board
- `{alg}` The algorithm used
- `{iter}` The number of iterations (unique number selections)
- `{depth}` The depth of the recursion tree when solved
- `{ms}` The time in ms to solve this board


- If the board is unsolvable, the program will instead print in this format:
```
Board: {board}
{alg}: Iterations={iter} 
Computation Time: {ms}ms
===(UNSOLVED)===
{}          {1...5.7.9} {12..5.7.9} {1..4....9} {12.45...9} {12..5...9} {}          {12.4....9} {}          
{.2..5..89} {}          {123.5..89} {}          {123.56..9} {123.56.89} {12..5...9} {12...6..9} {12..56..9} 
{.2..5.789} {1.3.5.789} {123.5.789} {1.34.6.89} {123456..9} {123.56.89} {12.45.7.9} {12.4.6..9} {12.4567.9} 
{.2.....89} {.......89} {.2.....89} {}          {1.3..6..9} {}          {123.....9} {}          {12...6..9} 
{}          {....5.789} {}          {}          {1....67.9} {1....6789} {1...5...9} {1....6.89} {1...56..9} 
{}          {....5.789} {}          {..3....89} {..3...7.9} {..3...789} {.2345...9} {.234...89} {.2.45...9} 
{...4..7.9} {}          {1.3...7.9} {1.34.6..9} {1.34.67.9} {1.3..67.9} {1.34..7.9} {}          {}          
{...45.7.9} {1.3.5.7.9} {1.3.5.7.9} {1.34....9} {}          {123.5.7.9} {}          {1234....9} {12.4..7.9} 
{...45.7.9} {}          {1.3.5.7.9} {1.34....9} {}          {123.5.7.9} {1234..7.9} {1234....9} {12.4..7.9} 
```
- This shows the state of how the board could be solved. It may not be apparent what is incorrect with the board

## Design Description
The following section describes the algorithms more in-depth and examines their performance

### Board Selection
When testing each algorithm, I used the file `test_boards.txt` which is provided in the submission folder
- Aside from the required 3 easy/medium/hard boards, I wanted to find more difficult boards for my program to solve
- Boards 1-9 are from [sudoku.com](https://sudoku.com/)
- Boards 10-12 are from [sudoku-solutions.com](https://www.sudoku-solutions.com/)
- Boards 13-21 are from [Leetcode Question 37](https://leetcode.com/problems/sudoku-solver/description/) and guarantee only one solution

### Simplification
The program takes three steps to simplify the problem before starting a search function:
1. For all positions, the possible answers are reduced by checking their initially solved row, column, and 3x3 grid
2. Any positions with exactly one possible answer are marked as solved. This repeats until a full pass of the board makes no changes, otherwise this step is repeated
3. Any position with a row/col/3x3 that contains a possible answer **exactly** once are marked as solved. After one board pass, step 2 is run again. Once a pass of the board in this step makes no changes, then initial simplification is complete

- For easier puzzles, the simplification step can produce a valid solution. When this fails, it is necessary to search for a solution
- For each possible answer of a position, a copy of the board is created and recursively searched
- Every new board is simplified by using step 3 (in the above section) before searching for the next position and possibilities
    - This simplification step greatly reduces the depth and branching factor of the search tree
- When a solution is identified, the search stops immediately and the solution is presented

### Depth First Search `dfs`
- DFS always makes the first possible choice it can make
- Each choice has the simplification algorithm run on it before further branching

### Minimum Remaining Value `mrv`
- At each step, MRV checks all unsolved positions to find the one with the least number of possible options
- This position recursively calls itself and tries all possible answers in order

### Least Constrained Value `lcv`
- At each step, LCV fills a priority queue
- Ordering is determined by which board has the largest number of permutations after simplification (product of the number of possibilities of all unsolved positions)
- These boards recursively calls themselves and try all possible boards in order

### Results Summary
- All three algorithms complete in roughly the same amount of time apart from leetcode3, where LCV is considerable slower
- The graph omits leetcode2 because the runtimes stretched the graph too much, especially LCV (6.03,11.06,73.46)

![runtimes.png](images%2Fruntimes.png)