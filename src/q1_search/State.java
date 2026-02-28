package q1_search;

import java.util.Arrays;

public class State {
    public int[] board; // 9 elements. 0 represents 'B'

    public State(int[] board) {
        this.board = board.clone();
    }

    // Finds the index of the blank space (0)
    public int getBlankIndex() {
        for (int i = 0; i < board.length; i++) {
            if (board[i] == 0) return i;
        }
        return -1;
    }

    // Crucial for the 'Visited' list to prevent infinite loops in DFS/BFS
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        State state = (State) obj;
        return Arrays.equals(board, state.board);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(board);
    }

    // Helper to print the state like "1 2 3 / 4 5 6 / 7 B 8"
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < board.length; i++) {
            sb.append(board[i] == 0 ? "B" : board[i]);
            if ((i + 1) % 3 == 0 && i < 8) sb.append(" / ");
            else if (i < 8) sb.append(" ");
        }
        return sb.toString();
    }
}