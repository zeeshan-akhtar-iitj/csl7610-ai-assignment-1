package q1_search;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainQ1 {

    public static State goalState; // We store the goal state globally for easy access

    public static void main(String[] args) {
        // 1. Read the file and get the root state
        State startState = parseInputFile("input.txt");

        if (startState == null || goalState == null) {
            System.out.println("Error reading the input file. Please check input.txt format.");
            return;
        }

        System.out.println("--- 8-PUZZLE PARSER ---");
        System.out.println("Start State: " + startState);
        System.out.println("Goal State:  " + goalState);
        System.out.println("-----------------------\n");

        // 2. Run the Algorithms!
        System.out.println("Running Uninformed Searches...");
        SearchAlgorithms.runBFS(new Node(startState), goalState);
        // Warning: DFS might take a very long time depending on the start state!
        SearchAlgorithms.runDFS(new Node(startState), goalState);

        System.out.println("\nRunning Informed Searches...");
        SearchAlgorithms.runAStar(new Node(startState), goalState, 1); // h1: Misplaced
        SearchAlgorithms.runAStar(new Node(startState), goalState, 2); // h2: Manhattan

        System.out.println("\nRunning Memory-Bounded & Local Searches...");
        SearchAlgorithms.runIDAStar(new Node(startState), goalState, 2); // Using Manhattan
        SearchAlgorithms.runSimulatedAnnealing(new Node(startState), goalState);
    }

    // --- SUCCESSOR FUNCTION ---
    public static List<Node> getSuccessors(Node parent) {
        List<Node> children = new ArrayList<>();
        int blankIndex = parent.state.getBlankIndex();

        // The 1D array indices look like this mapped to a 3x3 grid:
        // 0 1 2
        // 3 4 5
        // 6 7 8

        // Move UP: Blank goes up, meaning index decreases by 3 (Valid if blank is not in the top row)
        if (blankIndex >= 3) {
            children.add(createChild(parent, blankIndex, blankIndex - 3, "Up"));
        }
        // Move DOWN: Blank goes down, meaning index increases by 3 (Valid if blank is not in the bottom row)
        if (blankIndex <= 5) {
            children.add(createChild(parent, blankIndex, blankIndex + 3, "Down"));
        }
        // Move LEFT: Blank goes left, meaning index decreases by 1 (Valid if blank is not in the left column)
        if (blankIndex % 3 != 0) {
            children.add(createChild(parent, blankIndex, blankIndex - 1, "Left"));
        }
        // Move RIGHT: Blank goes right, meaning index increases by 1 (Valid if blank is not in the right column)
        if (blankIndex % 3 != 2) {
            children.add(createChild(parent, blankIndex, blankIndex + 1, "Right"));
        }

        return children;
    }

    // Helper to swap tiles and create a new Node
    private static Node createChild(Node parent, int blankIndex, int targetIndex, String action) {
        int[] newBoard = parent.state.board.clone();
        // Swap the blank with the target tile
        newBoard[blankIndex] = newBoard[targetIndex];
        newBoard[targetIndex] = 0; // 0 represents the blank

        State newState = new State(newBoard);
        // Step cost is always 1 unit of System Energy as per assignment
        return new Node(newState, parent, action, 1);
    }

    // --- FILE PARSER ---
    public static State parseInputFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine();
            if (line != null && line.startsWith("Start state:")) {

                // Split the string to separate start and goal halves
                String[] parts = line.split("Goal state:");

                // Extract only alphanumeric characters (removes spaces, semicolons, etc.)
                String startStr = parts[0].replaceAll("[^0-9B]", "");
                String goalStr = parts[1].replaceAll("[^0-9B]", "");

                goalState = new State(stringToBoard(goalStr));
                return new State(stringToBoard(startStr));
            }
        } catch (IOException e) {
            System.out.println("File not found! Make sure input.txt is in the root of your project.");
        }
        return null;
    }

    // Helper to convert "123B46758" into int array [1,2,3,0,4,6,7,5,8]
    private static int[] stringToBoard(String str) {
        int[] board = new int[9];
        for (int i = 0; i < 9; i++) {
            char c = str.charAt(i);
            board[i] = (c == 'B') ? 0 : Character.getNumericValue(c);
        }
        return board;
    }
}